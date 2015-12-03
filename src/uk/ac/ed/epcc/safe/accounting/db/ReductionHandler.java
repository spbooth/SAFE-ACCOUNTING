//| Copyright - The University of Edinburgh 2015                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.IndexReduction;
import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionProducer;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.model.data.DataObject;

/** A wrapper round an {@link ExpressionTargetFactory} that implements {@link ReductionProducer}
 * 
 * @author spb
 *
 * @param <E> type of {@link ExpressionTarget}
 */
public class ReductionHandler<E extends DataObject&ExpressionTarget,F extends ExpressionTargetFactory<E>> implements ReductionProducer<E> {

	public ReductionHandler(F fac) {
		super();
		this.map = fac.getAccessorMap();
		this.fac = fac;
	}

	private final AccessorMap map;
	private final F fac;
	
	private final BaseFilter<E> getFilter(RecordSelector selector) throws CannotFilterException {
		if( selector == null ){
			return null;
		}
		try {
			return selector.visit(new FilterSelectVisitor<E>(fac));
		}catch(CannotFilterException e){
			throw e;
		} catch (Exception e) {
			throw new CannotFilterException(e);
		}
	}
	private <X> boolean compatible(ReductionTarget<X> t){
		if(fac.compatible(t.getExpression())){
			return true;
		}
		if( t instanceof IndexReduction){
			// index reductions must resolve
			return false;
		}
		// other reductions can be null
		return true;
	}
	private boolean compatible(Set<ReductionTarget> list){
		for(ReductionTarget<?> t: list){
			if( ! compatible(t)){
				return false;
			}
		}
		return true;
	}
	public final boolean compatible(RecordSelector sel){
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(null,fac,false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public  Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap( Set<ReductionTarget> sum, RecordSelector selector) throws Exception{
		if( ! (fac.compatible(selector) && compatible(sum))){
			// can't do anything
			return new HashMap<ExpressionTuple, ReductionMapResult>();
		}
		try{
			IndexReductionFinder<E> finder = new IndexReductionFinder<E>(map, sum,makeDef(sum));
			return finder.find(FilterConverter.convert(getFilter(selector)));
		}catch(CannotUseSQLException e){
			// default to iterating. e.g a non SQL filter or property
			Set<PropExpression> keys = new HashSet<PropExpression>();
			
			for(ReductionTarget r : sum){
				if(r.getReduction() == Reduction.INDEX){
					keys.add(r.getExpression());
				}
			}
			// Build by iterating over records.
			Map<ExpressionTuple, ReductionMapResult> result = new HashMap<ExpressionTuple, ReductionMapResult>();
			Iterator<E> it = fac.getIterator(selector);
			while(it.hasNext()){
				E rec = it.next();
				ExpressionTuple tup = new ExpressionTuple(keys, rec);
				Map<ReductionTarget,Object> old = result.get(tup);
				if( old == null ){
					ReductionMapResult dat = new ReductionMapResult();
					for(ReductionTarget r : sum){
						dat.put(r,rec.evaluateExpression(r.getExpression(),r.getDefault()));
					}
					result.put(tup, dat);
				}else{
					for(ReductionTarget r : sum ){
						old.put(r, r.combine(old.get(r), rec.evaluateExpression(r.getExpression(),r.getDefault())));
					}
				}
			}
			return result;
		}catch(Exception e1){
			throw e1;
		}
	}
	
	
	
	public <R> R getReduction(ReductionTarget<R> type, RecordSelector sel) throws Exception {
		if( ! fac.compatible(type.getExpression())){
			return type.getDefault();
		}
		try{
			SQLFilter<E> sql_fil = FilterConverter.convert(getFilter(sel));
			FilterReduction<E,R> fs = new FilterReduction<E,R>(map,type);
			R res = fs.find(sql_fil);
			if( res == null ){
				return type.getDefault();
			}
			return res;
		}catch(CannotUseSQLException e){
			return getReductionByIterating(type, sel);
		}
	}
	<R> R getReductionByIterating(ReductionTarget<R> type, RecordSelector sel)
			throws Exception, InvalidPropertyException {
		//TODO think about what to do if this is AVG
		// combine operation may or may not be wrong depending on
		// if time average.
		R result = type.getDefault();
		Iterator<E> it = fac.getIterator(sel);
		while(it.hasNext()){
			E o = it.next();
			result = type.combine(result, o.evaluateExpression(type.getExpression()));
		}
		
		return result;
	}
	
	
	
	
	
	public <I> Map<I, Number> getReductionMap(PropExpression<I> index,
			ReductionTarget<Number> property,  RecordSelector selector)
			throws Exception 
	{
		
		if( !(fac.compatible(index) && fac.compatible(property.getExpression())&& compatible(selector))){
			// no matching property
			return new HashMap<I,Number>();
			
		}
		
		
		if (Number.class.isAssignableFrom(property.getTarget())) {
			try{
				// By default we'll sum the Number value.
				MapReductionFinder<E,I> finder = new MapReductionFinder<E,I>(map,index, property);
				Map<I, Number> result = finder.find(FilterConverter.convert(getFilter(selector)));
				return result;
			}catch(CannotUseSQLException e){
				return getReductionMapByIterating(index, property, selector);
			}
		} 
		return null;
		
	}
	<I> Map<I, Number> getReductionMapByIterating(PropExpression<I> index,
			ReductionTarget<Number> property, RecordSelector selector)
			throws Exception, InvalidPropertyException {
		Map<I,Number> result = new HashMap<I, Number>();
		Iterator<E> it = fac.getIterator(selector);
		while(it.hasNext()){
			E o = it.next();
			I ind = o.evaluateExpression(index);
			Number old = result.get(ind);
			if( old == null ){
				old = property.getDefault();
			}
			result.put(ind, property.combine(old, o.evaluateExpression(property.getExpression())));
		}
		
		return result;
	}

	
	/** Make a set of default values for the various reduction targets.
	 * 
	 * @param set
	 * @return
	 */
	private ReductionMapResult makeDef(Set<ReductionTarget> set){
		ReductionMapResult result = new ReductionMapResult();
		for(ReductionTarget t : set){
			Object def = t.getDefault();
			if( def != null ){
				result.put(t,def);
			}else{
				if( t.getTarget() == String.class && t.getReduction() == Reduction.INDEX){
					result.put(t,"Unknown");
				}
			}
		}
		return result;
	}

}