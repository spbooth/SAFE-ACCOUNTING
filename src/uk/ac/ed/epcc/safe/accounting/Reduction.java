// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.webapp.content.Operator;

/** Reduction operators.
 * <p>
 * The INDEX reduction is equivalent to the SQL GROUP BY CLAUSE, when combined with other
 * Reductions it requests that multiple results should be returned, with reductions only happending
 * across sets of records where the INDEX property is the same.
 * </p>
 * <p>
 * AVG is a mean value except when doing overlap mapping when it becomes a time average
 * </p>
 * @author spb
 *
 */
public enum Reduction {
  SUM(Operator.ADD),
  AVG(Operator.AVG),
  MIN(Operator.MIN),
  MAX(Operator.MAX),
  INDEX(Operator.MERGE);
  
  private final Operator op;
  private Reduction(Operator o){
	  this.op=o;
  }
  /** get a {@link Operator} suitable for combing partial results.
   * 
   * @return {@link Operator}
   */
  public Operator operator(){
	  return op;
  }
}