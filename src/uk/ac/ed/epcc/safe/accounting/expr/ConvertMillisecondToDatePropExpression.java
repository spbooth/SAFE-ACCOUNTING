package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

import java.util.Date;

public class ConvertMillisecondToDatePropExpression implements
		PropExpression<Date> {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((milli_expr == null) ? 0 : milli_expr.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConvertMillisecondToDatePropExpression other = (ConvertMillisecondToDatePropExpression) obj;
		if (milli_expr == null) {
			if (other.milli_expr != null)
				return false;
		} else if (!milli_expr.equals(other.milli_expr))
			return false;
		return true;
	}
	public final PropExpression<? extends Number> milli_expr;
	public ConvertMillisecondToDatePropExpression(PropExpression<? extends Number> millis) {
		this.milli_expr=millis;
	}
	public Class<? super Date> getTarget() {
		return Date.class;
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<R>)vis).visitConvetMillisecondToDateExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	public PropExpression<Date> copy() {
		return this;
	}
	public String toString(){
		return "Date("+milli_expr+")";
	}
	public PropExpression<? extends Number> getMillisecondExpression() {
		return milli_expr;
	}

}
