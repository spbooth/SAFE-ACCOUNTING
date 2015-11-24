// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
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