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
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;

/** A {@link ReductionTarget} operating on Dates.
 * 
 * @author spb
 *
 */



public class DateReductionTarget extends ReductionTarget<Date,Date> {

	public DateReductionTarget(Reduction op,
			PropExpression<? extends Date> tag) throws IllegalReductionException {
		super(Date.class,Date.class, op, tag);
		if(  op == Reduction.SUM || op == Reduction.AVG){
			throw new IllegalReductionException("Bad operation on date "+op);
		}
	}
	
}