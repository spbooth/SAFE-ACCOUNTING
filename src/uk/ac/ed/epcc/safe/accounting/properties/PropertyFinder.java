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

/** Interface for locating properties by name
 * 
 * @author spb
 *
 */
public interface PropertyFinder {

	/** Locate a registered property by name.
	 * The property is required to exist and an InvalidPropertyException will be thrown if it does not.
	 * Note that this is only able to find a property once the class where the property is defined has been loaded
	 * 
	 * @param name
	 * @return PropertyTag
	 * @throws InvalidPropertyException 
	 */
	public abstract PropertyTag<?> make(String name)
			throws InvalidPropertyException;

	/** Locate a registered property by name.
	 * Note that this is only able to find a property once the class where the property is defined has been loaded
	 * 
	 * @param name
	 * @return PropertyTag or null
	 */
	public abstract PropertyTag<?> find(String name);
	
	/** Locate a registered property by name and class of result.
	 * This method will only return a PropertyTag if its property can be assigned to
	 * class T
	 * 
	 * This is primarily intended to distinguish between tags with the same unqualified name.
	 * 
	 * @param <T>  Target class
	 * @param clazz Class object for target
	 * @param name String name of property
	 * @return PropertyTag or null
	 */
	public abstract <T> PropertyTag<? extends T> find(Class<T> clazz, String name );
		
	/** Locate a registered property by name and additional filter.
	 * This method will only return a {@link PropertyTag} if the {@link TagFilter}
	 * accepts the result.
	 * 
	 * @param fil
	 * @param name
	 * @return ProeprtyTag or null
	 */
	public abstract PropertyTag<?> find(TagFilter fil, String name);
	/** Get all properties That could be found using this object
	 * 
	 * @return Set of PropertyTag
	 */
	public abstract Set<PropertyTag> getProperties();
	
	/** is the specified property in the set of properties that can be found by this object.
	 * 
	 * @param t
	 * @return boolean
	 */
	public boolean hasProperty(PropertyTag t);
	
	/** Get all properties where the property can be assigned to class T
	 * 
	 * @param <T>
	 * @param clazz
	 * @return Set of PropertyTag
	 */
	public abstract <T> Set<PropertyTag<? extends T>> getProperties(Class<T> clazz);
	
	/** return a new PropertyFinder that represents a copy of the current state of this object.
	 *  
	 * @return PropertyFinder
	 */
    public PropertyFinder copy();
    
}