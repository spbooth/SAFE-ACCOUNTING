// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.model.data.Duration;
@uk.ac.ed.epcc.webapp.Version("$Id: DurationPropExpression.java,v 1.9 2014/09/15 14:32:21 spb Exp $")

/** A {@link PropExpression} that calculates a duration from two date expressions.
 * 
 * @author spb
 *
 */
public class DurationPropExpression implements PropExpression<Duration> {

	
	public final PropExpression<Date> start;
	public final PropExpression<Date> end;
	public DurationPropExpression(PropExpression<Date> start, PropExpression<Date> end){
		this.start=start.copy();
		this.end=end.copy();
	}
	public Class<? super Duration> getTarget() {
		return Duration.class;
	}

	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
		return ((PropExpressionVisitor<R>)vis).visitDurationPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	@Override
	public String toString() {
		return "Duration("+start.toString()+","+end.toString()+")";
	}
	public DurationPropExpression copy() {
		return this;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
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
		DurationPropExpression other = (DurationPropExpression) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

}