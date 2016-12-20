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


/** PropertyContainer is a container of property values keyed by PropertyTags.
 * Some PropertyContainers only support a fixed subset of the possible properties in which case get/set
 * operations may throw an InvalidPropertyException.
 * 
 *  Implementations of this interface should check that the supplied data is allowed by the specified PropertyTag
 *  and throw a ClassCastException if this check fails.
 * 
 * @author spb
 *
 */
public interface PropertyContainer extends PropertyTarget {

	/** Is the specified property supported by the container.
	 * If the container is unconstrained this always returns true
	 * 
	 * @param tag
	 * @return boolean true if property supported
	 */
	public boolean supports(PropertyTag<?> tag);
	/** Is the specified property supported for write.
	 * If the container is unconstrained this always returns true
	 * 
	 * @param tag
	 * @return boolean true if property can be written
	 */
	public boolean writable(PropertyTag<?> tag);
	/** Get the specified property from the container.
	 * THe InvalidPropertyException should only be thrown if the supports method would have
	 * returned false for the requested property.
	 * 
	 * @param <T> type of property
	 * @param key  PropertyTag identifying property
	 * @return value current value of property (may be null if not set).
	 * @throws InvalidExpressionException 
	 */
	public abstract <T> T getProperty(PropertyTag<T> key) throws InvalidExpressionException;
     /** Set the specified property
      * 
      * @param <T> type of property
      * @param key PropertyTag identifying property
      * @param value 
      * @throws InvalidPropertyException
      */
	public abstract <T> void setProperty(PropertyTag<? super T> key, T value) throws InvalidPropertyException;
    /** Set the specified property if it is supported
     * 
     * @param <T> type of property
     * @param key PropertyTag identifying property
     * @param value 
     */
	public abstract <T> void setOptionalProperty(PropertyTag<? super T> key, T value);
	/** Generate a set of all the properties defined in this container
	 * 
	 * @return Set
	 */
	public abstract Set<PropertyTag> getDefinedProperties();
	/** Copy all compatible properties from another container to this one;
	 * 
	 * @param source PropertyContainer
	 */
	public abstract void setAll(PropertyContainer source);
	
	/** clear all contents before disposal
	 * 
	 */
	public void release();
}