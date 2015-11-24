// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** PropExpression that converts a Date property to 
 * a millisecond value from the normal unix epoch.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: MilliSecondDatePropExpression.java,v 1.10 2014/09/15 14:32:21 spb Exp $")

public class MilliSecondDatePropExpression implements PropExpression<Long> {
   private final PropExpression<Date> date_expr;
   public MilliSecondDatePropExpression(PropExpression<Date> date) throws PropertyCastException{
	   if( ! Date.class.isAssignableFrom(date.getTarget())){
		   throw new PropertyCastException("Non date expression in MilliSecondDatePropExpression");
	   }
	   date_expr=date.copy();
   }
   public PropExpression<Date> getDateExpression(){
	   return date_expr;
   }
   @Override
   public String toString(){
	   return "Millis("+date_expr.toString()+")";
   }

public Class<Long> getTarget() {
	return Long.class;
}
public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
	if( vis instanceof PropExpressionVisitor){
	return ((PropExpressionVisitor<R>)vis).visitMilliSecondDatePropExpression(this);
	}
	throw new UnsupportedExpressionException(this);
	
}
@Override
public boolean equals(Object obj) {
	if( obj != null && obj.getClass() == getClass()){
		MilliSecondDatePropExpression peer = (MilliSecondDatePropExpression) obj;
		return date_expr.equals(peer.date_expr);
	}
	return false;
}
@Override
public int hashCode() {
	return date_expr.hashCode();
}
public MilliSecondDatePropExpression copy() {
	return this;
}
}