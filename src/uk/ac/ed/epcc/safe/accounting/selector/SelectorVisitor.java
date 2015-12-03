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
package uk.ac.ed.epcc.safe.accounting.selector;



/** Visitor class for RecordSelector
 * 
 * Bottom level classes that implement RecordSelector are visited directly. 
 * The methods on the collection classes will normally be implemented by recursing 
 * down to the SelectClause
 * 
 * 
 * This interface assumes that RecordSelectors are made from And/Or combinations of
 * SelectClause objects. If the data model for RecordSelectors is extended this interface
 * will also need to be extended but this should become obvious when implementing the 
 * visit method of the extended RecordSelector
 * 
 * @see RecordSelector
 * @see SelectClause
 * @see AndRecordSelector
 * @see OrRecordSelector
 * @see RelationClause
 * 
 * @author spb
 *
 * @param <R>
 */
public interface SelectorVisitor<R> {
	public R visitAndRecordSelector(AndRecordSelector a) throws Exception;
	public R visitOrRecordSelector(OrRecordSelector o) throws Exception;
	public <I> R visitClause(SelectClause<I> c) throws Exception;
	public <I> R visitNullSelector(NullSelector<I> n) throws Exception;
	public <I> R visitRelationClause(RelationClause<I> c) throws Exception;
	public R visitPeriodOverlapRecordSelector(PeriodOverlapRecordSelector o) throws Exception;
	public <I> R visitOrderClause(OrderClause<I> o ) throws Exception;
	public R visitReductionSelector(ReductionSelector r) throws Exception;
}