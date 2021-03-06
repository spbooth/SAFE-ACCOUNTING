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
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.IndexReduction;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionProducer;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;

/** A wrapper round an {@link ExpressionTargetGenerator} that implements {@link ReductionProducer}
 * 
 * @author spb
 *
 * @param <E> type of target
 * @param <F> type of {@link ExpressionTargetGenerator}
 */
public class GeneratorReductionHandler<E,F extends ExpressionTargetGenerator<E>> extends AbstractContexed implements ReductionProducer<E> {

	public static final Feature DISTINCT_BY_ITER = new Feature("reduction.distinct_by_iterating",true,"Allow DISTINCT reduction by iterating");
	public GeneratorReductionHandler(AppContext conn,F fac) {
		super(conn);
		
		this.fac = fac;
	}

	
	protected final F fac;
	
   
	protected final <X> boolean compatible(ReductionTarget<?,X> t){
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
	protected final boolean compatible(Set<ReductionTarget> list){
		for(ReductionTarget<?,?> t: list){
			if( ! compatible(t)){
				return false;
			}
		}
		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	public  Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap( Set<ReductionTarget> sum, RecordSelector selector) throws Exception{
		if( ! compatible(sum)){
			// can't do anything
			return new HashMap<>();
		}
			// default to iterating. e.g a non SQL filter or property
			Set<PropExpression> keys = new HashSet<>();
			
			for(ReductionTarget r : sum){
				if(r.getReduction() == Reduction.INDEX){
					keys.add(r.getExpression());
				}
				if( r.getReduction() == Reduction.DISTINCT && ! DISTINCT_BY_ITER.isEnabled(getContext())) {
					throw new UnsupportedReductionException("Cannot perform DISTINCT reduction by iteration");
				}
			}
			// Build by iterating over records.
			Map<ExpressionTuple, ReductionMapResult> result = new HashMap<>();
			try(CloseableIterator<ExpressionTargetContainer> it = fac.getExpressionIterator(selector)){
				while(it.hasNext()){
					ExpressionTargetContainer rec = it.next();
					ExpressionTuple tup = new ExpressionTuple(keys, rec);
					Map<ReductionTarget,Object> old = result.get(tup);
					if( old == null ){
						ReductionMapResult dat = new ReductionMapResult();
						for(ReductionTarget r : sum){
							dat.put(r,r.map(rec.evaluateExpression(r.getExpression(),r.getDefault())));
						}
						result.put(tup, dat);
					}else{
						for(ReductionTarget r : sum ){
							old.put(r, r.combine(old.get(r), r.map(rec.evaluateExpression(r.getExpression(),r.getDefault()))));
						}
					}
					rec.release();
				}
			}
			return result;
	}
	
	
	
	public <R,D> R getReduction(ReductionTarget<R,D> type, RecordSelector sel)
			throws Exception, InvalidPropertyException {
		
		if( type.getReduction() == Reduction.DISTINCT && ! DISTINCT_BY_ITER.isEnabled(getContext())) {
			throw new UnsupportedReductionException("Cannot perform DISTINCT reduction by iteration");
		}
		//TODO think about what to do if this is AVG
		// combine operation may or may not be wrong depending on
		// if time average.
		R result = type.getDefault();
		try(CloseableIterator<ExpressionTargetContainer> it = fac.getExpressionIterator(sel)){
			while(it.hasNext()){
				ExpressionTargetContainer et = it.next();
				result = type.combine(result, type.map(et.evaluateExpression(type.getExpression())));
				et.release();
			}
		}
		return result;
	}
	
	
	
	
	
	public <I,T,D> Map<I, T> getReductionMap(PropExpression<I> index,
			ReductionTarget<T,D> property, RecordSelector selector)
			throws Exception, InvalidPropertyException {
		if( property.getReduction() == Reduction.DISTINCT && ! DISTINCT_BY_ITER.isEnabled(getContext())) {
			throw new UnsupportedReductionException("Cannot perform DISTINCT reduction by iteration");
		}
		Map<I,T> result = new HashMap<>();
		try(CloseableIterator<ExpressionTargetContainer> it = fac.getExpressionIterator(selector)){
			while(it.hasNext()){
				ExpressionTargetContainer et = it.next();
				I ind = et.evaluateExpression(index);
				T old = result.get(ind);
				if( old == null ){
					old = property.getDefault();
				}
				result.put(ind, property.combine(old, property.map(et.evaluateExpression(property.getExpression()))));
				et.release();
			}
		}
		return result;
	}

	
	/** Make a set of default values for the various reduction targets.
	 * 
	 * @param set
	 * @return
	 */
	protected ReductionMapResult makeDef(Set<ReductionTarget> set){
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