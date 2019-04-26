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

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** PropExpression that converts a Date property to 
 * a millisecond value from the normal unix epoch.
 * 
 * @author spb
 *
 */


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