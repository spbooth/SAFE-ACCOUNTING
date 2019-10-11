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
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;



public class ExpressionAcceptMatchFilter<T,I> extends AbstractAcceptFilter<T>{
	private final AccessorMap<T> map;
	private final MatchCondition m;
	private final PropExpression<I> expr1;
	private final PropExpression<I> expr2;
	public ExpressionAcceptMatchFilter(Class<T> target,AccessorMap<T> map,PropExpression<I> expr1,MatchCondition m, PropExpression<I> expr2){
		super(target);
		this.map=map;
		this.expr1=expr1;
		this.m=m;
		this.expr2=expr2;
	}
	@SuppressWarnings("unchecked")
	public boolean accept(T t) {
		try {
			ExpressionTarget o = map.getContainer(t);
			if( o == null) {
				return false;
			}
			I res1 = o.evaluateExpression(expr1);
			I res2 = o.evaluateExpression(expr2);
			if( m !=null && (res1 == null || res1 == null)) {
				return false;
			}
			if( m == null ){
				if( res1 == null){
					return res2==null;
				}
				return res1.equals(res2);
			}
			return m.compare(res1,res2 );
		} catch (InvalidExpressionException e) {
			return false;
		}
	}

}