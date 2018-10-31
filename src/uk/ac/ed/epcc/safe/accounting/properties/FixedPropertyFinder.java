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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;


/** A PropertyFinder containing a fixed set of properties from a single namespace
 * Typically used to implement a {@link PropertyRegistry}
 * 
 * @author spb
 *
 */


public class FixedPropertyFinder implements PropertyFinder {
	public static final String PROPERTY_FINDER_NAME_REGEXP="[a-zA-Z]\\w*";
	public static final String PROPERTY_FINDER_SEPERATOR=":";
	public static final String PROPERTY_FINDER_PREFIX_REGEXP=PROPERTY_FINDER_NAME_REGEXP+PROPERTY_FINDER_SEPERATOR;

	private static final Pattern prefix_pattern=Pattern.compile(PROPERTY_FINDER_PREFIX_REGEXP);
	protected final Map<String,PropertyTag> registry;
	protected final String name;
	private final String description; // run-time documentation
	private final String prefix;
	public FixedPropertyFinder(String name,String description,Map<String,PropertyTag> map){
		this.name=name;
		this.description=description;
		prefix=name+PROPERTY_FINDER_SEPERATOR;
		if( ! prefix_pattern.matcher(prefix).matches()){
			throw new ConsistencyError("Illegal name for Propertyfinder "+name);
		}
		this.registry=map;
	}
	public PropertyFinder copy() {
		return this;
	}
	public String getDescription(){
		return description;
	}

	public Iterator<Entry<String, PropertyTag>> getIterator(){
		return this.registry.entrySet().iterator();
	}
	
	public PropertyTag<?> find(String name) {
		if( name == null ){
			return null;
		}
		if( name.startsWith(prefix)){
			// allow fully qualified lookups
			name=name.substring(prefix.length());
		}
		if( registry.containsKey(name)){
			return registry.get(name);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> PropertyTag<? extends T> find(Class<T> clazz, String name) {
		PropertyTag<?> res = find(name);
		if( res != null ){
			if( clazz.isAssignableFrom(res.getTarget())){
				return (PropertyTag<? extends T>) res;
			}else{
				//System.out.println("Tag "+res.getFullName()+" "+res.getTarget().getCanonicalName()+" not assignable to "+clazz.getCanonicalName());
			}
		}else{
			//System.out.println("tag "+name+" not found in "+this.name);
		}
		return null;

	}
	public PropertyTag<?> find(TagFilter fil, String name) {
		PropertyTag<?> res = find(name);
		if( res != null ){
			if( fil.accept(res)){
				return res;
			}else{
				//System.out.println("Tag "+res.getFullName()+" "+res.getTarget().getCanonicalName()+" not assignable to "+clazz.getCanonicalName());
			}
		}else{
			//System.out.println("tag "+name+" not found in "+this.name);
		}
		return null;

	}
	public Set<PropertyTag> getProperties() {
		return new HashSet<>(registry.values());

	}
	public boolean hasProperty(PropertyTag tag){
		return registry.containsValue(tag);
	}
	@SuppressWarnings("unchecked")
	public <T> Set<PropertyTag<? extends T>> getProperties(Class<T> clazz) {
		Set<PropertyTag<? extends T>> result = new LinkedHashSet<>();
		for(PropertyTag t : registry.values()){
			if( clazz.isAssignableFrom(t.getTarget())){
				result.add(t);
			}
		}
		
		return result;
	}

	public PropertyTag<?> make(String name) throws InvalidPropertyException {
		if( name != null ){
			if( name.startsWith(prefix)){
				// allow fully qualified lookups
				name=name.substring(prefix.length());
			}
			if( registry.containsKey(name)){
				return registry.get(name);
			}
		}
		throw new UnresolvedNameException(name,this);

	}
	public int size(){
		return registry.size();
	}
	@Override
	public String toString(){
		return name;
	}
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		 if( obj == this ){
			 return true;
		 }
		if( obj.getClass() == getClass()){
			FixedPropertyFinder reg = (FixedPropertyFinder) obj;
			return name.equals(reg.name);
		}
		return false;
	}
	protected String getPrefix(){
		return prefix;
	}
}