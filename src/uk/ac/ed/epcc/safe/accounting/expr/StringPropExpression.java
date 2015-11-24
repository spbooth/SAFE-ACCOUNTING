// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** A cast to to string expression.
 * 
 * @author spb
 *
 * @param <T>
 */
@uk.ac.ed.epcc.webapp.Version("$Id: StringPropExpression.java,v 1.12 2014/09/15 14:32:22 spb Exp $")

public class StringPropExpression<T> implements PropExpression<String> {
    public final PropExpression<T> exp;
    public StringPropExpression(PropExpression<T> e){
    	this.exp=e.copy();
    }
   	public PropExpression<T> getExpression(){
   		return exp;
   	}
	public Class<? super String> getTarget() {
		return String.class;
	}

	@Override
	public String toString(){
		return "String("+exp.toString()+")";
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
		return ((PropExpressionVisitor<R>)vis).visitStringPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	@Override
	public boolean equals(Object obj) {
		if( obj != null && obj.getClass() == getClass()){
			StringPropExpression peer = (StringPropExpression) obj;
			return exp.equals(peer.exp);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return exp.hashCode();
	}
	public StringPropExpression<T> copy() {
		return this;
	}
}