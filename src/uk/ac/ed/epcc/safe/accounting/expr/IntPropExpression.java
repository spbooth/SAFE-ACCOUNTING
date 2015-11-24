// Copyright - The University of Edinburgh 2011
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
@uk.ac.ed.epcc.webapp.Version("$Id: IntPropExpression.java,v 1.7 2014/09/15 14:32:21 spb Exp $")

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