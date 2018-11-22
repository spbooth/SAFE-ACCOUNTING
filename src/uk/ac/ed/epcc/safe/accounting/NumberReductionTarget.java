//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;

public abstract class NumberReductionTarget extends ReductionTarget<Number,Number> {

	public NumberReductionTarget(Reduction op,
			PropExpression<? extends Number> tag)
			throws IllegalReductionException {
		super(Number.class, Number.class,op, tag);
	}

	public static NumberReductionTarget getInstance(Reduction op, PropExpression<? extends Number> tag) throws IllegalReductionException{
		switch(op){
			case AVG: return new NumberAverageReductionTarget( tag);
			case SUM: return new NumberSumReductionTarget(tag);
			case MIN: return new NumberMinReductionTarget(tag);
			case MAX: return new NumberMaxReductionTarget(tag);
			default: throw new IllegalReductionException("Unsupported Number reduction "+op);
		}
	}

}