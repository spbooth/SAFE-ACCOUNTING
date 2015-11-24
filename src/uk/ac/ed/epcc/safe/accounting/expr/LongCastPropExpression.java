// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** A cast to long expression. 
 * This is only intended to allow generic number expressions to
 * be used where long values are required. It does not necessarily
 * imply truncation.
 * @author spb
 *
 * @param <T>
 */
@uk.ac.ed.epcc.webapp.Version("$Id: LongCastPropExpression.java,v 1.1 2015/04/07 12:28:42 spb Exp $")

public class LongCastPropExpression<T> implements PropExpression<Long> {
    public final PropExpression<T> exp;
    public LongCastPropExpression(PropExpression<T> e){
    	this.exp=e.copy();
    }
    public PropExpression<T> getExpression(){
   		return exp;
   	}
	public Class<? super Long> getTarget() {
		return Long.class;
	}

	@Override
	public String toString(){
		return "Long("+exp.toString()+")";
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
		return ((PropExpressionVisitor<R>)vis).visitLongCastPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	@Override
	public boolean equals(Object obj) {
		if( obj != null && obj.getClass() == getClass()){
			LongCastPropExpression peer = (LongCastPropExpression) obj;
			return exp.equals(peer.exp);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return exp.hashCode();
	}
	public LongCastPropExpression<T> copy() {
		return this;
	}
}