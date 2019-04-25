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
package uk.ac.ed.epcc.safe.accounting.reference;

import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;

/** Registry of all possible table references taken from the config.
 * 
 * All instances of this registry are equivalent as it is build from the config state.
 * Properties are named after the table tag.
 * 
 * Set <em><b>make_table_reference.</b>table=false</em> to supress a table
 * @author spb
 *
 */


public final class ReferencePropertyRegistry extends PropertyRegistry {

	public static final String REFERENCE_REGISTRY_NAME = "table";
	private static final String REFERENCE_PROPERTY_REGISTRY = "ReferencePropertyRegistry";

	@SuppressWarnings("unchecked")
	private ReferencePropertyRegistry(AppContext conn) {
		super(REFERENCE_REGISTRY_NAME, "Table references");
		uk.ac.ed.epcc.webapp.logging.Logger log = conn.getService(LoggerService.class).getLogger(getClass());
		Map<String,Class> map = conn.getClassMap(DataObjectFactory.class);
		for(String name : map.keySet()){
			if( conn.getBooleanParameter("make_table_reference."+name, true)){
				if( PropertyTag.name_pattern.matcher(name).matches()){
					new ReferenceTag(this, name, map.get(name),name);
				}else{
					log.warn("table name "+name+" not valid as a PropertyTag name");
				}
			}
		}
		lock();
	}

	public static ReferencePropertyRegistry getInstance(AppContext c){
		ReferencePropertyRegistry ref = (ReferencePropertyRegistry) c.getAttribute(REFERENCE_PROPERTY_REGISTRY);
		if( ref == null ){
			ref = new ReferencePropertyRegistry(c);
			c.setAttribute(REFERENCE_PROPERTY_REGISTRY, ref);
		}
		return ref;
	}
}