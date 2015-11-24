// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
/** Binary numerical operation on PropExpressions
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: BinaryPropExpression.java,v 1.18 2014/09/15 14:32:21 spb Exp $")

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
		return ( a.equals(peer.a) && b.equals(peer.b) && op.equals(peer.op));
	}
	return false;
}

@Override
public int hashCode() {
	return a.hashCode() + op.hashCode() + b.hashCode();
}

public BinaryPropExpression copy() {
	return this;
}

}