// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr.parse;


public interface ExpressionLexTarget extends LexTarget {

	/** get the parser token
	 * 
	 * @param pattern
	 * @return token
	 */
	public int getToken(String pattern);
}