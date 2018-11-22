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

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.Labeller;
/** A PropExpression that maps a nested expression to a String
 * using a {@link Labeller}
 * 
 * @author spb
 *
 * @param <T> Type of nested expression
 * @param <R> Return type of labeller
 */
public class LabelPropExpression<T,R> implements PropExpression<R> {

	private final Labeller<T,R> labeller;
	private final PropExpression<T> expr;
	

	public LabelPropExpression(Labeller<T,R> labeller, PropExpression<T> expr) {
		super();
		this.labeller = labeller;
		this.expr = expr;
	}
	@Override
	public Class<R> getTarget() {
		return labeller.getTarget();
	}
	@Override
	public <X> X accept(BasePropExpressionVisitor<X> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<X>)vis).visitLabelPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
		
	}
	@Override
	public PropExpression<R> copy() {
		return this;
	}
	
	public Labeller<T,R> getLabeller() {
		return labeller;
	}

	public PropExpression<T> getExpr() {
		return expr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result
				+ ((labeller == null) ? 0 : labeller.hashCode());
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
		LabelPropExpression other = (LabelPropExpression) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (labeller == null) {
			if (other.labeller != null)
				return false;
		} else if (!labeller.equals(other.labeller))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Label("+labeller.getClass().getSimpleName()+","+expr.toString()+")";
	}

}