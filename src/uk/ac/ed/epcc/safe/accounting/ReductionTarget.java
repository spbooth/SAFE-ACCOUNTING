// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** A ReductionTarget represents a {@link Reduction} of a {@link PropExpression} over set of records.
 * 
 * 
 * @author spb
 *
 * @param <T>
 */
public abstract class ReductionTarget<T> {
	private final Reduction op;
	private final PropExpression<? extends T> expr;
	
	private final Class<T> target;
	public ReductionTarget(Class<T> target, Reduction op, PropExpression<? extends T> tag) throws IllegalReductionException{
		
		this.op=op;
		this.expr=tag;
		this.target=target;
		if( ! target.isAssignableFrom(tag.getTarget())){
			throw new IllegalReductionException("Expression "+tag+" not compatible with "+target.getCanonicalName());
		}
	}
	
	public final Reduction getReduction(){
		return op;
	}
	
	public PropExpression<? extends T> getExpression(){
		return expr;
	}
	public final Class<T> getTarget(){
		return target;
	}
	/** Combine two partial values.
	 * The default value must be a legal input
	 * 
	 * @param a
	 * @param b
	 * @return combined result
	 */
	public abstract T combine(T a, T b);
	/** value to return if no records combined,
	 * 
	 * @return default value
	 */
	public T getDefault(){
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
	    if( obj instanceof ReductionTarget){
	    	ReductionTarget t = (ReductionTarget) obj;
	    	return t.op == op && t.expr.equals(expr);
	    }
		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return op.hashCode() + expr.hashCode();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return op.toString()+"("+expr.toString()+")";
	}
}