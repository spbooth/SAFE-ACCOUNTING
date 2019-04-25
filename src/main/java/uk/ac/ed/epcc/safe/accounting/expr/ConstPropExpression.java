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

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;



/** A constant value
 * 
 * @author spb
 *
 * @param <N>
 */
public class ConstPropExpression<N> implements PropExpression<N> {
  public final N val;
  private final Class<N> target;
  public ConstPropExpression(Class<N> target,N n){
	  this.target=target;
	  val=n;
  }
  @Override
  public String toString(){
	  return val.toString();
  }

public Class<N> getTarget() {
	return target;
}

public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
	if( vis instanceof PropExpressionVisitor){
		return ((PropExpressionVisitor<R>)vis).visitConstPropExpression(this);
	}
	throw new UnsupportedExpressionException(this);
}

@Override
public boolean equals(Object obj) {
	if( obj != null && obj.getClass() == getClass()){
		ConstPropExpression peer = (ConstPropExpression) obj;
		return val.equals(peer.val);
	}
	return false;
}
@Override
public int hashCode() {
	return val.hashCode();
}
public ConstPropExpression<N> copy() {
	return this;
}
}