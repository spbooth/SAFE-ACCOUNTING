//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.properties;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/** A {@link PropertyFinder} that represents an arbitrary set of properties.
 * 
 * @author spb
 *
 */
public class SetPropertyFinder implements PropertyFinder {

	private Map<String,PropertyTag> qualified = new LinkedHashMap<>();
	private Map<String,PropertyTag> base = new HashMap<>();
	
	public SetPropertyFinder(){
		
	}
	public SetPropertyFinder(Set<PropertyTag> values){
		for(PropertyTag t: values){
			add(t);
		}
	}
	
	public void add(PropertyTag tag){
		qualified.put(tag.getFullName(), tag);
		base.put(tag.getName(), tag);
	}
	public PropertyTag<?> make(String name) throws InvalidPropertyException {
		PropertyTag<?> result = find(name);
		if( result == null){
			throw new UnresolvedNameException(name,this);
		}
		return result;
	}

	public PropertyTag<?> find(String name) {
		if( name == null){
			return null;
		}
		PropertyTag<?> result;
		result=base.get(name);
		if( result != null ){
			return result;
		}
		return qualified.get(name);

	}

	@SuppressWarnings("unchecked")
	public <T> PropertyTag<? extends T> find(Class<T> clazz, String name) {
		PropertyTag<?> candidate = find(name);
		if( candidate == null ){
			return null;
		}
		if( candidate.allowClass(clazz)){
			return (PropertyTag<? extends T>) candidate;
		}
		if( ! name.contains(FixedPropertyFinder.PROPERTY_FINDER_SEPERATOR)){
			// might have a tag hidden by the default
			for( PropertyTag t : qualified.values()){
				if( t.getName().equals(name) && t.allowClass(clazz)){
					return t;
				}
			}
		}
		return null;
	}
	public PropertyTag<?> find(TagFilter fil, String name) {
		PropertyTag<?> candidate = find(name);
		if( candidate == null ){
			return null;
		}
		if( fil.accept(candidate)){
			return candidate;
		}
		if( ! name.contains(FixedPropertyFinder.PROPERTY_FINDER_SEPERATOR)){
			// might have a tag hidden by the default
			for( PropertyTag t : qualified.values()){
				if( t.getName().equals(name) && fil.accept(candidate)){
					return t;
				}
			}
		}
		return null;
	}
	public Set<PropertyTag> getProperties() {
		return new LinkedHashSet<>(qualified.values());
	}

	public boolean hasProperty(PropertyTag t) {
		return qualified.values().contains(t);
	}

	

	@SuppressWarnings("unchecked")
	public <T> Set<PropertyTag<? extends T>> getProperties(Class<T> clazz) {
		LinkedHashSet<PropertyTag<? extends T>> result = new LinkedHashSet<>();
		for( PropertyTag t : qualified.values()){
			if(t.allowClass(clazz)){
				result.add(t);
			}
		}
		return result;
	}

	public PropertyFinder copy() {
		SetPropertyFinder result = new SetPropertyFinder();
		for(PropertyTag t : qualified.values()){
			result.add(t);
		}
		return result;
	}

	

}