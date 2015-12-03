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
package uk.ac.ed.epcc.safe.accounting;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.content.Table;
/** Generates human readable summary of how properties are implemented for
 * a particular {@link UsageProducer}
 * 
 * @author spb
 *
 */

public class PropertyInfoMaker {
	
	private final UsageProducer<?> producer;
	
	public PropertyInfoMaker(UsageProducer<?> producer) {
		this.producer = producer;
	}
	
	public Set<PropertyTag> getProperties() {
		return producer.getFinder().getProperties();
		
	}
	
	
	
	public Set<PropertyTag> getSupportedProperties() {
		return getSupportedProperties(getProperties());

	}
	
	
	
	public Set<PropertyTag> getSupportedProperties(Set<PropertyTag> properties) {
		Set<PropertyTag> supportedProperties =  new HashSet<PropertyTag>();
		for (PropertyTag<?> tag : properties) {
			if (producer.hasProperty(tag)) {
				supportedProperties.add(tag);				
			}
		}
		return supportedProperties;
		
	}
	
	public Table getTable() {
		return getTable(getProperties());
	}
	
	public Table getTable(Set<PropertyTag> properties) {
		Table<String,PropertyTag> t = new Table<String,PropertyTag>();
		for (PropertyTag tag : properties) {
			t.put("Name", tag, tag.getFullName());
			// t.put("Class",tag,tag.getClass().getSimpleName());
			t.put("Type", tag, tag.getTarget().getSimpleName());
			t.put("Description", tag, tag.getDescription());
			t.put("Implementation", tag, producer.getImplemenationInfo(tag));
		}
		t.sortRows(new String[] { "Name", "Type" }, false);
		return t;

	}
	
}