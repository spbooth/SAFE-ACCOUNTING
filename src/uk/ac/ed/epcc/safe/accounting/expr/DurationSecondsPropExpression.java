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
import uk.ac.ed.epcc.webapp.model.data.Duration;
/** Convert {@link Duration} valued {@link PropExpression} into seconds.
 * 
 * @author spb
 *
 */
public class DurationSecondsPropExpression implements PropExpression<Number>{

	private final  PropExpression<Duration> expr;
	public DurationSecondsPropExpression(PropExpression<Duration> d) {
		this.expr=d;
	}

	public Class<Number> getTarget() {
		return Number.class;
	}
	public PropExpression<Duration> getDuration(){
		return expr;
	}

	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<R>)vis).visitDurationSecondPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}

	public PropExpression<Number> copy() {
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
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
		DurationSecondsPropExpression other = (DurationSecondsPropExpression) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}

}