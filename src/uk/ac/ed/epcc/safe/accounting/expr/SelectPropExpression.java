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
import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

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


public class SelectPropExpression<T> implements PropExpression<T> , Iterable<PropExpression<T>>{
	private final boolean use_any;
	private final PropExpression<T> alts[];
	private final Class<? super T> target;
	public SelectPropExpression(Class<? super T> target, PropExpression<T> alts[]){
		this(false,target,alts);
	}
	@SuppressWarnings("unchecked")
	public SelectPropExpression(boolean use_any,Class<? super T> target, PropExpression<T> alts[]){
		this.use_any = use_any;
		this.target=target;
		this.alts=new PropExpression[alts.length];
		for(int i=0;i<alts.length;i++){
			this.alts[i] = alts[i].copy();
		}
	}
	public boolean allowAny(){
		return use_any;
	}
	public int length(){
		return alts.length;
	}
	public PropExpression<T> get(int i){
		return alts[i];
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
		return ((PropExpressionVisitor<R>)vis).visitSelectPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}

	public Class<? super T> getTarget() {
		return target;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(" {");
		boolean seen=false;
		for(PropExpression e : alts){
			if( seen ){
				sb.append(" , ");
			}
			seen=true;
			sb.append(e.toString());
		}
		sb.append("} ");
		return sb.toString();
	}
	/** Utility class to generate SelectPropExpression with 
	 * a sensible target type.
	 * 
	 * @param e
	 * @return SelectPropExpression
	 */
	@SuppressWarnings("unchecked")
	public static SelectPropExpression makeSelect(PropExpression e[]){
		Class targets[]={String.class, IndexedReference.class, Double.class, Long.class, Integer.class, Float.class,Number.class, Date.class};
		target: for( Class c : targets){
			for( PropExpression exp :e){
				if( ! c.isAssignableFrom(exp.getTarget())){
					continue target;
				}
			}
			return new SelectPropExpression(c, e);
		}
		return new SelectPropExpression(Object.class, e);
	}
	@Override
	public boolean equals(Object obj) {
		if( obj != null && obj.getClass() == getClass()){
			SelectPropExpression peer = (SelectPropExpression) obj;
			if( alts.length != peer.alts.length){
				return false;
			}
			for(int i=0 ; i< alts.length ; i++){
				if( ! alts[i].equals(peer.alts[i])){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	@Override
	public int hashCode() {
		int result=0;
		for(int i=0;i<alts.length;i++){
			result += alts[i].hashCode();
		}
		return result;
	}
	public SelectPropExpression<T> copy() {

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
}