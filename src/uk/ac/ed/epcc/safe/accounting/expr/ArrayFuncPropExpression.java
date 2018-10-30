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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.ArrayFunc;

/** An expression that selects a result from a
 * list of expressions.
 * 
 * If the {@link #allowAny()} method returns true then all expressions are
 * equivalent and a later expression in the list can be used if necessary.
 * Otherwise each expression must be evaluated in turn until one
 * returns a non-null value. In this case it is not acceptable to skip an expression
 * that might be evaluated
 * @author spb
 *
 * @param <T>
 */


public class ArrayFuncPropExpression<T extends Comparable> implements PropExpression<T> , Iterable<PropExpression<T>>{
	private final ArrayFunc func;
	private final PropExpression<T> alts[];
	private final Class<T> target;
	
	
	@SuppressWarnings("unchecked")
	public ArrayFuncPropExpression(Class<T> target, ArrayFunc func,PropExpression<T> alts[]){
		
		this.target=target;
		this.func=func;
		this.alts=new PropExpression[alts.length];
		for(int i=0;i<alts.length;i++){
			this.alts[i] = alts[i].copy();
		}
	}
	
	public int length(){
		return alts.length;
	}
	public PropExpression<T> get(int i){
		return alts[i];
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
		return ((PropExpressionVisitor<R>)vis).visitArrayFuncPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}

	public Class<T> getTarget() {
		return target;
	}

	/**
	 * @return the func
	 */
	public ArrayFunc getFunc() {
		return func;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(func.name());
		sb.append("(");
		boolean seen=false;
		for(PropExpression e : alts){
			if( seen ){
				sb.append(" , ");
			}
			seen=true;
			sb.append(e.toString());
		}
		sb.append(")");
		return sb.toString();
	}
	/** Utility class to generate SelectPropExpression with 
	 * a sensible target type.
	 * 
	 * @param e
	 * @return SelectPropExpression
	 */
	@SuppressWarnings("unchecked")
	public static ArrayFuncPropExpression makeArrayFunc(ArrayFunc func,Collection<PropExpression> e){
		Class targets[]={ Double.class, Long.class, Integer.class, Float.class,Number.class, Date.class};
		target: for( Class c : targets){
			for( PropExpression exp :e){
				if( ! c.isAssignableFrom(exp.getTarget())){
					continue target;
				}
			}
			return new ArrayFuncPropExpression(c,func, e.toArray(new PropExpression[e.size()]));
		}
		return new ArrayFuncPropExpression(Object.class, func,e.toArray(new PropExpression[e.size()]));
	}
	
	public ArrayFuncPropExpression<T> copy() {

		return this;
	}
	public Iterator<PropExpression<T>> iterator() {
		return new Iterator<PropExpression<T>>() {
			private int pos=0;
			public boolean hasNext() {
				return pos < alts.length;
			}

			public PropExpression<T> next() {
				return alts[pos++];
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(alts);
		result = prime * result + ((func == null) ? 0 : func.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayFuncPropExpression other = (ArrayFuncPropExpression) obj;
		if (!Arrays.equals(alts, other.alts))
			return false;
		if (func != other.func)
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}
}