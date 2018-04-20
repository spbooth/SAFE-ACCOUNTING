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
import uk.ac.ed.epcc.webapp.jdbc.filter.AcceptFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;


/** An {@link AcceptFilter} based on evaluating expression as run-time.
 * 
 * @author spb
 *
 * @param <T> type filter is for
 * @param <I> data type
 */
public class ExpressionAcceptFilter<T,I> extends AbstractAcceptFilter<T>{
    private final AccessorMap<T> map;
	private final MatchCondition m;
	private final I data;
	private final PropExpression<I> expr;
	public ExpressionAcceptFilter(Class<? super T> target,AccessorMap<T> map,PropExpression<I> expr,MatchCondition m, I data){
		super(target);
		this.map=map;
		this.expr=expr;
		this.m=m;
		this.data=data;
		assert(data!=null);
	}
	public boolean accept(T t) {
		try {
			ExpressionTarget o = map.getProxy(t);
			if( o == null) {
				return false;
			}
			I res = o.evaluateExpression(expr);
			if( res==null || data == null) {
				return false;
			}
			if( m == null ){
				return data.equals(res);
			}
			boolean compare = m.compare(res, data);
			return compare;
		} catch (InvalidExpressionException e) {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "ExpressionAcceptFilter ["+expr+" "+ (m == null ? "=" : m.toString())+" "+data+"]";
	}
}