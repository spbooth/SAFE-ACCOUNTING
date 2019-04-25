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
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;

/** A class that supports  expressions over PropertyTags.
 * 
 * @author spb
 *
 */
public interface ExpressionTarget extends PropertyTarget{
	
	/** Get a {@link Parser} used to parse expressions for
	 * this target.
	 * 
	 * @return Parser
	 */
	public Parser getParser();
	
/** Evaluate an expression on the target object
 * 
 * @param <T> type of expression
 * @param expr expression
 * @return T
 * @throws InvalidExpressionException
 */
	public abstract <T> T evaluateExpression(PropExpression<T> expr)
			throws InvalidExpressionException;
	
	/** Evaluate expression with default. If the expression does not
	 * resolve then return the default value
	 * 
	 * @param <T> type of expression
	 * @param expr expression
	 * @param def default value
	 * @return T
	 */
	public abstract <T> T evaluateExpression(PropExpression<T> expr,T def);

}