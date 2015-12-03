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
package uk.ac.ed.epcc.safe.accounting.model;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.WebNameFinder;

/** Externally authenticated accounting {@link AppUser}.
 * 
 * @author spb
 *
 */



public class PropertyPerson extends AppUser implements PropertyContainer , ExpressionTarget{
   
    private final ExpressionTargetContainer proxy;
	@SuppressWarnings("unchecked")
	protected PropertyPerson(PropertyPersonFactory<? extends PropertyPerson> fac,Record res) {
		super(fac,res);
		AccessorMap map = fac.getAccessorMap();
		proxy = map.getProxy(this);
	}
	
	public PropertyPersonFactory getFactory(){
		return (PropertyPersonFactory) super.getFactory();
	}

	@Override
	public String getIdentifier(int max_length){
		String name= getRealmName(WebNameFinder.WEB_NAME);
		if( name == null){
			name = super.getIdentifier();
		}
		if( name == null ){
			name = "person-"+getID();
		}
		return name;
	}
	public final <T> T getProperty(PropertyTag<T> tag) throws InvalidExpressionException {
		return proxy.getProperty(tag);
	}
	public final <T> T getProperty(PropertyTag<T> tag, T def) {
		return proxy.getProperty(tag, def);
	}



	public final <T> void setProperty(PropertyTag<? super T> tag, T value) throws InvalidPropertyException {
		proxy.setProperty(tag, value);
	}
	public final <T> void setOptionalProperty(PropertyTag<? super T> tag, T value) {
		proxy.setOptionalProperty(tag, value);
	}
	public final <T> void setProperty(PropertyTag<T> tag, PropertyContainer map) throws InvalidExpressionException{
		setProperty(tag,map.getProperty(tag));
	}
	
	public final boolean supports(PropertyTag<?> tag){
		return proxy.supports(tag);
	}
	public final boolean writable(PropertyTag<?> tag){
		return proxy.writable(tag);
	}


	public <T> T evaluateExpression(PropExpression<T> expr)
			throws InvalidExpressionException {
		return proxy.evaluateExpression(expr);
	}
	public <T> T evaluateExpression(PropExpression<T> expr, T def){
		return proxy.evaluateExpression(expr,def);
	}

	public ExpressionTargetFactory getExpressionTargetFactory() {
		return getFactory();
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

	


	


	
}