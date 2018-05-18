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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
/** A map of Property values keyed by PropertyTag
 * 
 * This class provides a memory resident implementation of PropertyContainer that is capable of
 * storing any property. PropertyMap will therefore not throw InvalidPropertyException from the set methods.
 * 
 * @author spb
 *
 */


public class PropertyMap  implements PropertyContainer{
	private final LinkedHashMap<PropertyTag,Object> data= new LinkedHashMap<PropertyTag,Object>();
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.PropertyContainer#getProperty(uk.ac.ed.epcc.safe.accounting.PropertyTag)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProperty(PropertyTag<T> key){
		return (T) data.get(key);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.PropertyContainer#setProperty(uk.ac.ed.epcc.safe.accounting.PropertyTag, T)
	 */
	public final <T> void setProperty(PropertyTag<? super T> key, T value){
		// all properties optional for PropertyMap
		setOptionalProperty(key, value);
	}
	public final <T> void setOptionalProperty(PropertyTag<? super T> key, T value){
		if( ! key.allow(value)){
			throw new ClassCastException("Invalid object added to PropertyMap valueType="+(value==null?"null":value.getClass().getCanonicalName())+" not allowed by "+key.getFullName());
		}
		data.put(key, value);
	}
	/** Set all properties that can be written to a container
	 * 
	 * @param record
	 * @return number of properties set
	 */
    @SuppressWarnings("unchecked")
	public int setContainer(PropertyContainer record){
    	int count=0;
    	for(PropertyTag t : propertySet()){
    	
    		if( record.writable(t)){
    			count++;
    			Object property = getProperty(t);
    			// need explicit null check as DerivedPropertyMap will generate
    			// null of un-resolved derived property
    			if( property != null ){
    				record.setOptionalProperty(t, property);
    			}
    		}
		}
    	return count;
    }
	
	public final <T> T getProperty(PropertyTag<T> tag, T def) {
		// use getProperty to ensure subclasses work
		T val= (T) getProperty(tag);
		if( val == null){
			if( ! tag.allow(def)){
				throw new ClassCastException("Invalid object as default");
			}
			return def;
		}
		assert( tag.allow(val));
		return val;
	}
	public final boolean supports(PropertyTag<?> tag) { // final as setProperty assumes all tags valid
		return true;
	}
	public boolean writable(PropertyTag<?> tag) {
		return true;
	}
	
	public Set<PropertyTag> propertySet(){
		return data.keySet();
	}
	public int size(){
		return data.size();
	}
	public Set<PropertyTag> getDefinedProperties() {
		return new HashSet<PropertyTag>(data.keySet());
	}
	public void setAll(PropertyContainer source) {
		for(PropertyTag t : source.getDefinedProperties()){
			try {
				data.put(t, source.getProperty(t));
			} catch (InvalidExpressionException e) {
				//throw new ConsistencyError("Invalid property in defined list", e);
			}
		}
	}
	@Override
	public String toString() {
		return "PropertyMap ["+getContents() + "]";
	}
	protected String getContents() {
		return "data=" + data;
	}
	public void release(){
		data.clear();
	}
	
}