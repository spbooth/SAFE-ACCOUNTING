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
package uk.ac.ed.epcc.safe.accounting;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.webapp.Targetted;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseSQLFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.time.Period;
/** Interface for ExpressionTarget factories that can generate filters based on a {@link PropExpression} 
 * 
 * @author spb
 *
 * @param <T> type of factory target
 */
public interface ExpressionFilterTarget<T> extends Targetted<T>{

	/** get a filter based on comparing a PropExpression to a value.
	 * 
	 * @param <R> type of prop-expression
	 * @param expr expression to match
	 * @param match kind of match
	 * @param value to math against
	 * @return SQLFilter<T>
	 * @throws CannotFilterException 
	 */
	public abstract <R> BaseFilter<T> getFilter(PropExpression<R> expr,
			MatchCondition match, R value) throws CannotFilterException;
	/** get a filter that checks null status of an expression.
	 * 
	 * @param <R> type of prop-expression
	 * @param expr    expression to match
	 * @param is_null match if null when true
	 * @return Filter
	 * @throws CannotFilterException 
	 */
	public abstract <R> BaseFilter<T> getNullFilter(PropExpression<R> expr, boolean is_null) throws CannotFilterException;
	/** get a filter that compares two expressions.
	 * 
	 * @param left
	 * @param match
	 * @param right
	 * @return Filter
	 * @throws CannotFilterException
	 */
	public abstract <R> BaseFilter<T> getRelationFilter(PropExpression<R> left, MatchCondition match, PropExpression<R> right) throws CannotFilterException;
	/** Generate an overlap filter
	 * 
	 * @param period   Period to overlap
	 * @param start_prop start property
	 * @param end_prop   end property
	 * @param type OverlapType
	 * @param cutoff maximum length of records zero if not known
	 * @return Filter
	 * @throws CannotFilterException
	 */
	public BaseFilter<T> getPeriodFilter(Period period,
			PropExpression<Date> start_prop, 
			PropExpression<Date> end_prop, 
			OverlapType type,long cutoff)
			throws CannotFilterException;
	/** get an ordering {@link SQLFilter} based on the expression.
	 * 
	 * This will contain an {@link OrderFilter} but will also contain any required filters.
	 * @param descending
	 * @param expr
	 * @return {@link BaseSQLFilter}
	 * @throws CannotFilterException
	 */
	public <I> BaseSQLFilter<T> getOrderFilter(boolean descending, PropExpression<I> expr) throws CannotFilterException;
	
	/** get a {@link BaseFilter} which matches records where the current person
	 * has the specified named relationship with the record.
	 * 
	 * @param relationship
	 * @return {@link BaseFilter}
	 * @throws CannotFilterException
	 */
	public BaseFilter<T> getRelationshipFilter(String relationship) throws CannotFilterException;
}