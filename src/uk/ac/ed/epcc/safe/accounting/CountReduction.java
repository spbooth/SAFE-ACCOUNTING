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
import uk.ac.ed.epcc.webapp.DistinctCount;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;

/** A {@link ReductionTarget} that generates a count distinct reduction.
 * 
 * @author spb
 *
 */



public class CountReduction extends ReductionTarget<Number,Object> {


	@Override
	public Number map(Object o) {
		return DistinctCount.make(o);
	}




	public CountReduction(
			PropExpression<?> tag) throws IllegalReductionException {
		super(Number.class, Object.class,Reduction.DISTINCT,tag);
		
	}

	


	@Override
	public Number getDefault() {
		return DistinctCount.zero();
	}
}