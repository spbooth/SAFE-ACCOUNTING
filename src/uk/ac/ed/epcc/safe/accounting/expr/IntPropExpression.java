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
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** A cast to integer expression.
 * 
 * @author spb
 *
 * @param <T>
 */


public class IntPropExpression<T> implements PropExpression<Integer> {
    public final PropExpression<T> exp;
    public IntPropExpression(PropExpression<T> e){
    	this.exp=e.copy();
    }
    public PropExpression<T> getExpression(){
   		return exp;
   	}
	public Class<? super Integer> getTarget() {
		return Integer.class;
	}

	@Override
	public String toString(){
		return "Int("+exp.toString()+")";
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
		return ((PropExpressionVisitor<R>)vis).visitIntPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	@Override
	public boolean equals(Object obj) {
		if( obj != null && obj.getClass() == getClass()){
			IntPropExpression peer = (IntPropExpression) obj;
			return exp.equals(peer.exp);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return exp.hashCode();
	}
	public IntPropExpression<T> copy() {
		return this;
	}
}