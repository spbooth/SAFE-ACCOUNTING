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
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;

/** A ReductionTarget represents a {@link Reduction} of a {@link PropExpression} over set of records.
 * 
 * 
 * @author spb
 *
 * @param <T> type produces
 * @param <D> type of underlying data
 */
public abstract class ReductionTarget<T,D> {
	private final Reduction op;
	private final PropExpression<? extends D> expr;
	
	private final Class<T> target;
	public ReductionTarget(Class<T> target, Class<D> inner,Reduction op, PropExpression<? extends D> tag) throws IllegalReductionException{
		
		this.op=op;
		this.expr=tag;
		this.target=target;
		if( ! inner.isAssignableFrom(tag.getTarget())){
			throw new IllegalReductionException("Expression "+tag+" not compatible with "+target.getCanonicalName());
		}
	}
	
	public final Reduction getReduction(){
		return op;
	}
	
	public PropExpression<? extends D> getExpression(){
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
	public final T combine(T a, T b) {
		return (T) op.operator().operate(a, b);
	}
	/** map the underlying object to the reduction result type.
	 * This is only used when performing the reduction by iteration.
	 * normally the underlying type will be the same but mapping
	 * is needed for DISTINCT reductions by iteration.
	 * 
	 * @param o
	 * @return
	 */
	public T map(D o) {
		return (T) o;
	}
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