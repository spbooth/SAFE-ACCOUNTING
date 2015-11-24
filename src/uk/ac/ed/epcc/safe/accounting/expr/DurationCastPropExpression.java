// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.model.data.Duration;

/** A cast a numerical expression to {@link Duration}.
 * 
 * @author spb
 * @param <N> numerical type of base expression
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: DurationCastPropExpression.java,v 1.9 2014/09/15 14:32:21 spb Exp $")

public class DurationCastPropExpression<N extends Number> implements PropExpression<Duration> {
    public final PropExpression<N> exp; // underlying expression
    public final long resolution;       // resolution of exp in milliseconds
    public DurationCastPropExpression(PropExpression<N> e, long resolution){
    	this.exp=e.copy();
    	this.resolution=resolution;
    }
   	
	public Class<? super Duration> getTarget() {
		return Duration.class;
	}
 
	@Override
	public String toString(){
		return "Duration("+exp.toString()+","+resolution+")";
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<R>)vis).visitDurationCastPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	
	public DurationCastPropExpression<N> copy() {
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exp == null) ? 0 : exp.hashCode());
		result = prime * result + (int) (resolution ^ (resolution >>> 32));
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
		DurationCastPropExpression other = (DurationCastPropExpression) obj;
		if (exp == null) {
			if (other.exp != null)
				return false;
		} else if (!exp.equals(other.exp))
			return false;
		if (resolution != other.resolution)
			return false;
		return true;
	}
}