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
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.data.reference.ReferenceProvider;
/** Represents an additional table of data used to classify accounting records into sets.
 * This is achieved by having the accounting record reference an entry in the classification table.
 * 
 * AccountingClassification objects also have properties associated with them which can be accessed
 * in a PropExpression. 
 * 
 * Note that this can be constructed by both a {@link ParseAccountingClassificationFactory} or an {@link AccountingClassificationFactory} 
 * @author spb
 *
 */


public class AccountingClassification extends Classification implements PropertyContainer, ExpressionTarget , ReferenceProvider, ExpressionTargetContainer{
    protected final PropertyTargetClassificationFactory fac;
    protected final ExpressionTargetContainer proxy;
	@SuppressWarnings("unchecked")
	protected AccountingClassification(PropertyTargetClassificationFactory<?> fac,Record res) {
		super(res);
		this.fac=fac;
		AccessorMap map = fac.getAccessorMap();
		proxy = map.getProxy(this);
	}

	
    /* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ExpressionTarget#getProperty(uk.ac.ed.epcc.safe.accounting.PropertyTag)
	 */
	public <T> T getProperty(PropertyTag<T> tag, T def){
    	return proxy.getProperty(tag, def);
    }
    public final <T> void setProperty(PropertyTag<T> tag, PropertyContainer map) throws InvalidExpressionException{
		setProperty(tag,map.getProperty(tag));
	}
   
	public final <T> T getProperty(PropertyTag<T> tag) throws InvalidExpressionException {
		return proxy.getProperty(tag);
	}
   
	public final <T> void setProperty(PropertyTag<? super T> tag, T value) throws InvalidPropertyException {
		proxy.setProperty(tag, value);
	}
  
	public final <T> void setOptionalProperty(PropertyTag<? super T> tag, T value) {
		proxy.setOptionalProperty(tag, value);
	}
	public final boolean supports(PropertyTag<?> tag){
		return proxy.supports(tag);
	}
	public final boolean writable(PropertyTag<?> tag){
		return proxy.writable(tag);
	}
    /* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ExpressionTarget#evaluateExpression(uk.ac.ed.epcc.safe.accounting.expr.PropExpression)
	 */
	public <T> T evaluateExpression(PropExpression<T> expr) throws InvalidExpressionException{
    	return proxy.evaluateExpression(expr);
    }
	public <T> T evaluateExpression(PropExpression<T> expr,T def){
    	return proxy.evaluateExpression(expr,def);
    }

	public ExpressionTargetFactory getExpressionTargetFactory() {
		return fac;
	}

	@SuppressWarnings("unchecked")
	public PropertyTargetClassificationFactory<? extends AccountingClassification> getFactory(){
		return fac;
	}

	@SuppressWarnings("unchecked")
	public IndexedReference getReference() {
		return ((DataObjectFactory)fac).makeReference(this);
	}


	public Set<PropertyTag> getDefinedProperties() {
		return proxy.getDefinedProperties();
	}


	public void setAll(PropertyContainer source) {
		proxy.setAll(source);
	}


	public Parser getParser() {
		return proxy.getParser();
	}


	@SuppressWarnings("unchecked")
	public Object getKey() {
		return fac.makeReference(this);
	}

	
	@Override
	public void release(){
		super.release();
		proxy.release();
	}

}