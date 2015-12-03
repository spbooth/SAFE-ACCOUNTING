//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.LinkedList;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
/** a {@link PropExpression} for a case statement where the expression 
 * generated is selected by a set of {@link RecordSelector}s.
 * 
 * @author spb
 *
 * @param <T> type of expression
 */
public class CasePropExpression<T> implements PropExpression<T>{

	public static class Case<T>{
		/**
		 * @param sel
		 * @param expr
		 */
		public Case(RecordSelector sel, PropExpression<? extends T> expr) {
			super();
			this.sel = sel;
			this.expr = expr;
		}
		public final RecordSelector sel;
		public final PropExpression<? extends T> expr;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((expr == null) ? 0 : expr.hashCode());
			result = prime * result + ((sel == null) ? 0 : sel.hashCode());
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
			Case other = (Case) obj;
			if (expr == null) {
				if (other.expr != null)
					return false;
			} else if (!expr.equals(other.expr))
				return false;
			if (sel == null) {
				if (other.sel != null)
					return false;
			} else if (!sel.equals(other.sel))
				return false;
			return true;
		}
	}
	
	private final Class<T> target;
	private final PropExpression<? extends T> default_expression;
	private final LinkedList<Case<T>> cases;
	public CasePropExpression(Class<T> target, PropExpression<? extends T> def, Case<T> ... args ) {
		this.target=target;
		this.default_expression=def;
		cases=new LinkedList<CasePropExpression.Case<T>>();
		for( Case<T> c : args){
			cases.add(c);
		}
	}

	public LinkedList<CasePropExpression.Case<T>> getCases(){
		return new LinkedList<CasePropExpression.Case<T>>(cases);
	}
	public PropExpression<? extends T> getDefaultExpression(){
		return default_expression;
	}
	public Class<? super T> getTarget() {
		return target;
	}

	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<R>)vis).visitCasePropExpression(this);
			}
			throw new UnsupportedExpressionException(this);
	}

	public PropExpression<T> copy() {
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cases == null) ? 0 : cases.hashCode());
		result = prime
				* result
				+ ((default_expression == null) ? 0 : default_expression
						.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		CasePropExpression other = (CasePropExpression) obj;
		if (cases == null) {
			if (other.cases != null)
				return false;
		} else if (!cases.equals(other.cases))
			return false;
		if (default_expression == null) {
			if (other.default_expression != null)
				return false;
		} else if (!default_expression.equals(other.default_expression))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

}