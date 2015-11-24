// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
@uk.ac.ed.epcc.webapp.Version("$Id: DeRefExpression.java,v 1.20 2014/09/25 11:22:50 spb Exp $")

/** A {@link PropExpression} that de-references a {@link ReferenceExpression} then
 * evaluates an expression on the referenced object.
 * 
 * @author spb
 *
 * @param <R> remote type
 * @param <T> target type
 */
public class DeRefExpression<R extends DataObject & ExpressionTarget,T> implements PropExpression<T> , FormatProvider{

	private final ReferenceExpression<R> target_object;
	private final PropExpression<T> expr;
	public DeRefExpression(ReferenceExpression<R> tag, PropExpression<T> expr){
		this.target_object=tag.copy();
		this.expr=expr.copy();
	}
	public ReferenceExpression<R> getTargetObject(){
		return target_object;
	}
	
	public Class<? super T> getTarget() {
		return expr.getTarget();
	}
	public PropExpression<T> getExpression(){
		return expr;
	}
	@Override
	public String toString(){
		return target_object.toString()+"["+expr.toString()+"]";
	}
	public <X> X accept(BasePropExpressionVisitor<X> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<X>)vis).visitDeRefExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}

	@Override
	public boolean equals(Object obj) {
		if( obj != null && obj.getClass() == getClass()){
			DeRefExpression peer = (DeRefExpression) obj;
			return target_object.equals(peer.target_object) && expr.equals(peer.expr);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return target_object.hashCode() + expr.hashCode();
	}
	public DeRefExpression<R, T> copy() {
	
		return this;
	}
	public Labeller getLabeller() {
		if( expr instanceof FormatProvider){
			return ((FormatProvider)expr).getLabeller();
		}
		return null;
	}
}