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
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionProducer;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;

/** A wrapper round an {@link ExpressionTargetFactory} that implements {@link ReductionProducer}
 * It uses SQL reductions if it can but defaults to iterating 
 * (as per the {@link GeneratorReductionHandler} superclass if it can't.
 * 
 * @see GeneratorReductionHandler
 * @author spb
 *
 * @param <E> type of record
 */
public class ReductionHandler<E,F extends ExpressionTargetFactory<E>> extends GeneratorReductionHandler<E, F> {

	public ReductionHandler(F fac) {
		super(fac);
		this.map = fac.getAccessorMap();
		
	}

	private boolean qualify=false;
	private final AccessorMap map;
	
	
	protected final BaseFilter<E> getFilter(RecordSelector selector) throws CannotFilterException {
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
	
	public final boolean compatible(RecordSelector sel){
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(null,fac.getAccessorMap(),false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			return false;
		}
	}
	public final boolean compatible(PropExpression e) {
		return map.resolves(e, false);
	}
	@SuppressWarnings("unchecked")
	public  Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap( Set<ReductionTarget> sum, RecordSelector selector) throws Exception{
		if( ! (compatible(selector) && compatible(sum))){
			// can't do anything
			return new HashMap<ExpressionTuple, ReductionMapResult>();
		}
		try{
			IndexReductionFinder<E> finder = new IndexReductionFinder<E>(map, sum,makeDef(sum));
			if( isQualify()) {
				finder.setQualify(true);
			}
			return finder.find(FilterConverter.convert(getFilter(selector)));
		}catch(CannotUseSQLException e){
			// default to iterating. e.g a non SQL filter or property
			return super.getIndexedReductionMap(sum, selector);
		}catch(Exception e1){
			throw e1;
		}
	}
	
	
	
	public <R> R getReduction(ReductionTarget<R> type, RecordSelector sel) throws Exception {
		if( ! map.resolves(type.getExpression(),false)){
			return type.getDefault();
		}
		try{
			SQLFilter<E> sql_fil = FilterConverter.convert(getFilter(sel));
			FilterReduction<E,R> fs = new FilterReduction<E,R>(map,type);
			if( isQualify()) {
				fs.setQualify(true);
			}
			R res = fs.find(sql_fil);
			if( res == null ){
				return type.getDefault();
			}
			return res;
		}catch(CannotUseSQLException e){
			return super.getReduction(type, sel);
		}
	}

	
	public <I> Map<I, Number> getReductionMap(PropExpression<I> index,
			ReductionTarget<Number> property,  RecordSelector selector)
			throws Exception 
	{
		
		if( !(compatible(index) && compatible(property.getExpression())&& compatible(selector))){
			// no matching property
			return new HashMap<I,Number>();
			
		}
		
		
		if (Number.class.isAssignableFrom(property.getTarget())) {
			try{
				// By default we'll sum the Number value.
				MapReductionFinder<E,I> finder = new MapReductionFinder<E,I>(map,index, property);
				if( isQualify()) {
					finder.setQualify(true);
				}
				Map<I, Number> result = finder.find(FilterConverter.convert(getFilter(selector)));
				return result;
			}catch(CannotUseSQLException e){
				return super.getReductionMap(index, property, selector);
			}
		} 
		return null;
		
	}

	/**
	 * @return the qualify
	 */
	public boolean isQualify() {
		return qualify;
	}

	/**
	 * @param qualify the qualify to set
	 */
	public boolean setQualify(boolean qualify) {
		boolean old=isQualify();
		this.qualify = qualify;
		return old;
	}
	
	
	
}