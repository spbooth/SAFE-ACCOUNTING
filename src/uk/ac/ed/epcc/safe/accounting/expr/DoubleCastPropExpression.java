// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** A cast to double expression.
 * 
 * @author spb
 *
 * @param <T> type being cast
 */
@uk.ac.ed.epcc.webapp.Version("$Id: DoubleCastPropExpression.java,v 1.1 2015/04/07 12:28:41 spb Exp $")

public class DoubleCastPropExpression<T> implements PropExpression<Double> {
    public final PropExpression<T> exp;
    public DoubleCastPropExpression(PropExpression<T> e){
    	this.exp=e.copy();
    }
    public PropExpression<T> getExpression(){
   		return exp;
   	}
	public Class<? super Double> getTarget() {
		return Double.class;
	}

	@Override
	public String toString(){
		return "Double("+exp.toString()+")";
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<R>)vis).visitDoubleCastPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	@Override
	public boolean equals(Object obj) {
		if( obj != null && obj.getClass() == getClass()){
			DoubleCastPropExpression peer = (DoubleCastPropExpression) obj;
			return exp.equals(peer.exp);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return exp.hashCode();
	}

	public DoubleCastPropExpression<T> copy() {
		return this;
	}
}