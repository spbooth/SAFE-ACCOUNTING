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
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.parsers.TypeConverterPropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.model.data.convert.TypeConverter;
/** PropExpression that allows an arbitrary conversion.
 * 
 * Note that these cannot be converted to an {@link uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression} so you have to explicitly
 * implement PropExpression directly if there is an equivalent SQL mapping
 * 
 * @author spb
 * @see TypeConverterPropertyTag
 * @param <T>  target data-type
 * @param <D> underlying data-type
 *  */


public class TypeConverterPropExpression<T, D> implements PropExpression<T> {

	private final TypeConverter<T, D> converter;
	private final PropExpression<D> inner;
	private final Class<T> target;
	public TypeConverterPropExpression(Class<T> target,TypeConverter<T, D> converter, PropExpression<D> expr){
		this.target=target;
		this.converter=converter;
		this.inner=expr.copy();
		assert(this.converter !=null);
		assert(this.inner != null);
	}
	public Class<T> getTarget() {
		return target;
	}
	public TypeConverter<T, D> getConverter(){
		return converter;
	}
	public PropExpression<D> getInnerExpression(){
		return inner;
	}

	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
		return ((PropExpressionVisitor<R>)vis).visitTypeConverterPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
	public TypeConverterPropExpression<T,D> copy() {
		return this;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((converter == null) ? 0 : converter.hashCode());
		result = prime * result + ((inner == null) ? 0 : inner.hashCode());
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
		TypeConverterPropExpression other = (TypeConverterPropExpression) obj;
		if (converter == null) {
			if (other.converter != null)
				return false;
		} else if (!converter.equals(other.converter))
			return false;
		if (inner == null) {
			if (other.inner != null)
				return false;
		} else if (!inner.equals(other.inner))
			return false;
		return true;
	}
	public String toString(){
		return "("+inner.toString()+")=>"+getTarget().getSimpleName();
	}

}