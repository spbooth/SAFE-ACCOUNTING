// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;

@uk.ac.ed.epcc.webapp.Version("$Id: ConstPropExpression.java,v 1.14 2014/09/15 14:32:21 spb Exp $")

/** A constant value
 * 
 * @author spb
 *
 * @param <N>
 */
public class ConstPropExpression<N> implements PropExpression<N> {
  public final N val;
  private final Class<? super N> target;
  public ConstPropExpression(Class<? super N> target,N n){
	  this.target=target;
	  val=n;
  }
  @Override
  public String toString(){
	  return val.toString();
  }

public Class<? super N> getTarget() {
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