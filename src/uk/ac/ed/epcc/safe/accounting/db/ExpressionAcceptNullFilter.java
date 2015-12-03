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
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.AcceptFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterVisitor;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;



public class ExpressionAcceptNullFilter<T extends ExpressionTarget,I> implements AcceptFilter<T>{
	private final Class<? super T> target;
	private final boolean is_null;
	private final PropExpression<I> expr;
	
	public ExpressionAcceptNullFilter(Class<? super T> target,PropExpression<I> expr, boolean is_null){
		this.target=target;
		this.expr=expr;
		this.is_null=is_null;
	}

	public boolean accept(T o) {
		try {
			I res = o.evaluateExpression(expr);
			if( res == null ){
				return is_null;
			}
			if( res instanceof IndexedReference){
				return is_null == ((IndexedReference)res).isNull();
			}
			return ! is_null;
		} catch (InvalidExpressionException e) {
			return is_null;
		}
	}

	public <X> X acceptVisitor(FilterVisitor<X, ? extends T> vis) throws Exception {
		return vis.visitAcceptFilter(this);
	}

	public Class<? super T> getTarget() {
		return target;
	}


}