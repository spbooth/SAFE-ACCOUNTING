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
package uk.ac.ed.epcc.safe.accounting.selector;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;



public final class NullSelector<T> implements RecordSelector{
	public final PropExpression<T> expr;
	public final boolean is_null;
	
	public NullSelector(PropExpression<T> exp, boolean is_null){
		this.expr=exp.copy();
		this.is_null=is_null;
	}
	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		return visitor.visitNullSelector(this);
	}
	public NullSelector<T> copy() {
		return this;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + (is_null ? 1231 : 1237);
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
		NullSelector other = (NullSelector) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (is_null != other.is_null)
			return false;
		return true;
	}

}