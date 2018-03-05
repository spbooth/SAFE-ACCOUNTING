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
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.model.data.DataObject;


/** Interface for Parse targets that represent mutable data.
 * Existing entries are updated each time they are parsed.
 * 
 * @author spb
 *
  * @param <T> Type of record parsed to
 * @param <R> Type of intermediate record (type input is split into) for parse
 */
public interface UploadParseTarget<T extends DataObject & PropertyContainer,R> extends
		PropertyContainerParseTarget<T,R> {
	/** Returns an uncommitted object updated to match the input container.
	 * This is either a new object or an existing object selected using the
	 * key properties.
	 * 
	 * @param value
	 * @return new or existing object 
	 * @throws AccountingParseException 
	 * 
	 */
	public T make(PropertyContainer value) throws AccountingParseException;
	
	/** extract global properties from post parameters.
	 * 
	 * @param params
	 * @return PropertyMap
	 */
	public PropertyMap getGlobals(Map<String,Object> params);

}