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
package uk.ac.ed.epcc.safe.accounting.properties;

import java.util.Set;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;

/** A PropertyTuple represents a set of PropertyTags and associated values.
 * 
 * A PropertyTuple will only equal another if the contents are equivalent.
 * 
 * @author spb
 *
 */


public class PropertyTuple implements PropertyContainer {
	private final PropertyMap map;
	private int hash=0;
	public PropertyTuple(PropertyMap values) throws InvalidExpressionException {
		this(values.propertySet(),values);
	}
	
	@SuppressWarnings("unchecked")
	public PropertyTuple(Set<PropertyTag> use,PropertyContainer values) throws InvalidExpressionException{
		map = new PropertyMap();
		for(PropertyTag t : use){
			Object v = values.getProperty(t);
			if( v != null ){
				map.setProperty(t, v);
				hash+=v.hashCode();
			}
		}
	}
	public int size(){
		return map.size();
	}
    public Set<PropertyTag> propertySet(){
    	return map.propertySet();
    }
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if( obj == this ){
			return true;
		}
		if( ! (obj instanceof PropertyTuple)){
			return false;
		}
		PropertyTuple t = (PropertyTuple) obj;
		if( t.hashCode() != hash || t.map.size() != map.size()){
			return false;
		}
		for(PropertyTag<?> tag: map.propertySet()){
			if( ! map.getProperty(tag).equals(t.map.getProperty(tag))){
				return false;
			}
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash;
	}

	public <T> T getProperty(PropertyTag<T> key)
			throws InvalidPropertyException {
		return map.getProperty(key);
	}

	public <T> void setOptionalProperty(PropertyTag<? super T> key, T value) {
		
		
	}

	public <T> void setProperty(PropertyTag<? super T> key, T value)
			throws InvalidPropertyException {
		throw new InvalidPropertyException(key);
		
	}

	public boolean supports(PropertyTag<?> tag) {
		return map.supports(tag);
	}

	public <T> T getProperty(PropertyTag<T> tag, T def) {
		return map.getProperty(tag, def);
	}

	public boolean writable(PropertyTag<?> tag) {
		return false;
	}
	public Set<PropertyTag> getDefinedProperties() {
		return map.getDefinedProperties();
	}
	public void setAll(PropertyContainer source) {
		throw new ConsistencyError("invalid operation for PropertyTuple");
	}
	
	
}