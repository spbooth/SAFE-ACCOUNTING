// Copyright - The University of Edinburgh 2011
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
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyMap.java,v 1.7 2015/03/18 11:43:35 spb Exp $")

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
		return "PropertyMap [data=" + data + "]";
	}
}