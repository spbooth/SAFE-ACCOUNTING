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

import java.util.Date;

public class ConvertMillisecondToDatePropExpression implements
		PropExpression<Date> {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((milli_expr == null) ? 0 : milli_expr.hashCode());
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
		ConvertMillisecondToDatePropExpression other = (ConvertMillisecondToDatePropExpression) obj;
		if (milli_expr == null) {
			if (other.milli_expr != null)
				return false;
		} else if (!milli_expr.equals(other.milli_expr))
			return false;
		return true;
	}
	public final PropExpression<? extends Number> milli_expr;
	public ConvertMillisecondToDatePropExpression(PropExpression<? extends Number> millis) {
		this.milli_expr=millis;
	}
	public Class<? super Date> getTarget() {
		return Date.class;
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<R>)vis).visitConvetMillisecondToDateExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	public PropExpression<Date> copy() {
		return this;
	}
	public String toString(){
		return "Date("+milli_expr+")";
	}
	public PropExpression<? extends Number> getMillisecondExpression() {
		return milli_expr;
	}

}