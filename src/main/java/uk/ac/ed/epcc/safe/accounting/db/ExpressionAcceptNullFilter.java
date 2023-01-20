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
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.AbstractAcceptFilter;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;



public class ExpressionAcceptNullFilter<T,I> extends AbstractAcceptFilter<T>{
	private final AccessorMap<T> map;
	private final boolean is_null;
	private final PropExpression<I> expr;
	
	public ExpressionAcceptNullFilter(String target,AccessorMap<T> map,PropExpression<I> expr, boolean is_null){
		super(target);
		this.map=map;
		this.expr=expr;
		this.is_null=is_null;
	}

	public boolean test(T t) {
		try {
			ExpressionTarget o = map.getContainer(t);
			if( o == null) {
				return false;
			}
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
}