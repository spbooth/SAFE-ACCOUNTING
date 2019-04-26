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


/** An object where the internal state can be accessed as properties.
 * @see PropertyTargetFactory
 * 
 * @author spb
 *
 */
public interface PropertyTarget {

	/** Get property with default.
	 * If the property is not supported by the container of if no value has been set
	 * then the default value is returned.
	 * 
	 * @param <T> Type of property
	 * @param tag PropertTag identifying property
	 * @param def default value
	 * @return property value or default.
	 */
	public abstract <T> T getProperty(PropertyTag<T> tag, T def);

}