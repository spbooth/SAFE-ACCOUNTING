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
import uk.ac.ed.epcc.webapp.AverageValue;
import uk.ac.ed.epcc.webapp.NumberOp;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;

/** A {@link ReductionTarget} operating on numbers. 
 * 
 * @author spb
 *
 */



public class NumberSumReductionTarget extends NumberReductionTarget{
	private final Number def;
	public NumberSumReductionTarget(
			PropExpression<? extends Number> tag) throws IllegalReductionException {
		super( Reduction.SUM, tag);
		Class clazz = tag.getTarget();
		if( clazz == AverageValue.class){
			def = new AverageValue(0.0, 0L);
		}else if( clazz == Double.class || clazz == Number.class){
			def = Double.valueOf(0.0);
		}else if( clazz == Float.class){
			def = Float.valueOf(0.0F);
		}else if( clazz == Long.class){
			def = Long.valueOf(0L);
		}else if( clazz == Integer.class){
			def =  Integer.valueOf(0);
		}else{
		    def = Double.valueOf(0.0);
		}
	}
	public NumberSumReductionTarget( 
			PropExpression<? extends Number> tag,Number def) throws IllegalReductionException {
		super(Reduction.SUM, tag);
		this.def=def;
	}
	
	@Override
	public Number getDefault(){
		return def;
		
	}

}