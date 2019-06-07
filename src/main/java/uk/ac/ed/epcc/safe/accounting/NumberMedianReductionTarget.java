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

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.MedianValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;

/** A {@link ReductionTarget} operating on numbers that generate an
 * median.
 * The result is always a {@link MedianValue} object.
 * @author spb
 *
 */



public class NumberMedianReductionTarget extends NumberReductionTarget{
	public NumberMedianReductionTarget(
			PropExpression<? extends Number> tag) throws IllegalReductionException {
		super(Reduction.MEDIAN, tag);
	}
	

	@Override
	public Number getDefault(){
		return new MedianValue();
		
	}


	@Override
	public boolean isNativeType() {
		// We are generating a AverageValue
		return false;
	}

	@Override
	public boolean canUseSQL() {
		return false;
	}
}