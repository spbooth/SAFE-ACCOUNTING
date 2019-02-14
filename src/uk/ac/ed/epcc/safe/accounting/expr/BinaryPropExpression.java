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
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
/** Binary numerical operation on PropExpressions
 * <p>
 * If the operation is commutative then this is reflected in the {@link #equals(Object)} method.
 * @author spb
 *
 */


public class BinaryPropExpression implements PropExpression<Number> {
  public final PropExpression<? extends Number> a;
  public final Operator op;
  public final PropExpression<? extends Number> b;
  public BinaryPropExpression(PropExpression<? extends Number> a, Operator op, PropExpression<? extends Number> b) throws PropertyCastException{
	  if( ! Number.class.isAssignableFrom(a.getTarget()) || ! Number.class.isAssignableFrom(b.getTarget())){
		  throw new PropertyCastException("Non numeric arguments to BinaryExpression");
	  }
	  this.a=a.copy();
	  this.op=op;
	  this.b=b.copy();
  }
 
  @Override
  public String toString(){
	return format(a)+op.text()+format(b);  
  }
  private String format(PropExpression e){
	  if( e instanceof BinaryPropExpression){
		  return "("+e.toString()+")";
	  }else{
		  return e.toString();
	  }
  }

public Class<Number> getTarget() {
	return Number.class;
}

public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
	if( vis instanceof PropExpressionVisitor){
		return ((PropExpressionVisitor<R>)vis).visitBinaryPropExpression(this);
	}
	throw new UnsupportedExpressionException(this);
}

@Override
public boolean equals(Object obj) {
	if( obj != null && obj.getClass() == getClass()){
		BinaryPropExpression peer = (BinaryPropExpression) obj;
		if( op.commutes()) {
			// for commuting ops allows a,b swap to count as equals
			if( ! op.equals(peer.op)) {
				return false;
			}
			return ( a.equals(peer.a) && b.equals(peer.b)) || ( a.equals(peer.b) && b.equals(peer.a));
		}else {
			return ( a.equals(peer.a) && b.equals(peer.b) && op.equals(peer.op));
		}
	}
	return false;
}

@Override
public int hashCode() {
	// must be independent of a,b swap for communtative ops
	return a.hashCode() + op.hashCode() + b.hashCode();
}

public BinaryPropExpression copy() {
	return this;
}

}