// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr.parse;

 /** Target expression for the lexer
  * 
  * @author spb
  *
  */
public interface LexTarget {
	/** Specify a regular expression matching target
	 * 
	 * @return regexp String
	 */
  public String getRegexp();
  /** create the target object from the matched string.
   * 
   * @param pattern
   * @return target Object
 * @throws LexException 
   */
  public Object make(String pattern) throws LexException;
}