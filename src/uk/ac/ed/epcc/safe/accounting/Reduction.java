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
 * <p>
 * SELECT just selects a value without adding to the SQL GROUP BY. The expression is assumed to be
 * derivable from the INDEXs or the same for all records.
 * </p>
 * @author spb
 *
 */
public enum Reduction {
  SUM(Operator.ADD),
  AVG(Operator.AVG),
  MIN(Operator.MIN),
  MAX(Operator.MAX),
  INDEX(Operator.MERGE),
  SELECT(Operator.MERGE);
  
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