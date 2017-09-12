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
package uk.ac.ed.epcc.safe.accounting.selector;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
/** A {@link RecordSelector} that requests the results be generated in a particular order.
 * 
 * @author spb
 *
 * @param <T>
 */
public class OrderClause<T> implements RecordSelector {
	private final boolean descending;
	private final PropExpression<T> expr;
	
	public OrderClause(boolean descending,PropExpression<T> e) {
		this.descending=descending;
		this.expr=e;
	}

	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		return visitor.visitOrderClause(this);
	}

	public RecordSelector copy() {
		return this;
	}

	public PropExpression<T> getExpr() {
		return expr;
	}
	public boolean getDescending(){
		return descending;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (descending ? 1231 : 1237);
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
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
		OrderClause other = (OrderClause) obj;
		if (descending != other.descending)
			return false;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}

}