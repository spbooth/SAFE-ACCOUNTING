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
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** A de-reference expression that is itself a reference expression.
 * 
 * By using this class rather than a simple {@link DeRefExpression} there is more opportunity to implement as joins
 *  
 * @author spb
 *
 * @param <R> Type of first de-reference
 * @param <T> Type of target
 */


public class DoubleDeRefExpression<R extends DataObject & ExpressionTarget,T extends Indexed> extends DeRefExpression<R, IndexedReference<T>> implements ReferenceExpression<T>{

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression#accept(uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor)
	 */
	@Override
	public <X> X accept(BasePropExpressionVisitor<X> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<X>)vis).visitDoubleDeRefExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}

	public DoubleDeRefExpression(ReferenceExpression<R> tag,
			ReferenceExpression<T> expr) {
		super(tag, expr);
	}

	public ReferenceExpression<T> getNext(){
		return (ReferenceExpression<T>) getExpression();
	}
	public IndexedProducer<T> getFactory(AppContext c) {
		return getNext().getFactory(c);
	}

	public Class<? extends IndexedProducer> getFactoryClass() {
		return getNext().getFactoryClass();
	}

	public String getTable() {
		return getNext().getTable();
	}
	public DoubleDeRefExpression<R, T> copy(){
		return this;
	}
	
}