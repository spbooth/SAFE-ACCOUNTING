//| Copyright - The University of Edinburgh 2011                            |
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
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
/** visitor to convert a RecordSelector into a Filter
 * 
 */
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.NullSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OrRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OrderClause;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.ReductionSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RelationClause;
import uk.ac.ed.epcc.safe.accounting.selector.RelationshipClause;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.safe.accounting.selector.SelectorVisitor;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLOrFilter;



public class FilterSelectVisitor<T> implements SelectorVisitor<BaseFilter<T>>{

	/**
	 * 
	 */
	private final ExpressionFilterTarget<T> target;

	/**
	 * @param dataObjectPropertyFactory
	 */	
	public FilterSelectVisitor(
			ExpressionFilterTarget<T> dataObjectPropertyFactory) {
		target = dataObjectPropertyFactory;
	}
	public FilterSelectVisitor(
			ExpressionTargetFactory<T> dataObjectPropertyFactory) {
		target = dataObjectPropertyFactory.getAccessorMap();
	}
	public BaseFilter<T> visitAndRecordSelector(AndRecordSelector a) throws Exception {
		AndFilter<T> result = new AndFilter<T>(target.getTarget());
		for( RecordSelector s : a){
			assert(s!=null);
			result.addFilter(s.visit(this));
		}
		return result;
	}

	public SQLFilter<T> visitOrRecordSelector(OrRecordSelector o) throws Exception {
		SQLOrFilter<T> result = new SQLOrFilter<T>(target.getTarget());
		for( RecordSelector s : o){
			assert(s!=null);
			//try{
				result.addFilter(FilterConverter.convert(s.visit(this)));
			//}catch(CannotFilterException e){
				// convert an impossible branch into a false.
				// the other branches may still apply
			//}catch(NoSQLFilterException e){
				
			//}
		}
		return result;
	}

	public <I> BaseFilter<T> visitClause(SelectClause<I> c) throws CannotFilterException {
		assert(c != null);
		return target.getFilter(c.tag, c.match, c.data);
	}

	public <I> BaseFilter<T> visitNullSelector(NullSelector<I> n)
			throws Exception {
		assert(n != null);
		return target.getNullFilter(n.expr, n.is_null);
	}

	
	public <I> BaseFilter<T> visitRelationClause(RelationClause<I> c)
			throws Exception {
	
		return target.getRelationFilter(c.left, c.match, c.right);
	}

	public BaseFilter<T> visitPeriodOverlapRecordSelector(
			PeriodOverlapRecordSelector o) throws Exception {
		return target.getPeriodFilter(o.getPeriod(), o.getStart(), o.getEnd(),o.getType(),o.getCutoff());
	}

	public <I> BaseFilter<T> visitOrderClause(OrderClause<I> o) throws Exception {
		return target.getOrderFilter(o.getDescending(), o.getExpr());
	}

	public BaseFilter<T> visitReductionSelector(ReductionSelector r)
			throws Exception {
		AndFilter<T> fil = new AndFilter<T>(target.getTarget());
		for(ReductionTarget t : r){
			if( t.getReduction() == Reduction.INDEX ){
				// Index must not be null unless explicitly permitted
				fil.addFilter(target.getNullFilter(t.getExpression(), false));
			}
		}
		return fil;
	}

	@Override
	public  BaseFilter<T> visitRelationshipClause(
			RelationshipClause r) throws Exception {
		return target.getRelationshipFilter(r.getRelationship());
	}
	
}