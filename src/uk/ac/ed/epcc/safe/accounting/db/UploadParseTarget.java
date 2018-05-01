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

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;


/** Interface for Parse targets that represent mutable data.
 * Existing entries are updated each time they are parsed.
 * 
 *  The parse methods always return an {@link ExpressionTargetContainer}
 *  this interface may be directly implemented by the record type or it may
 *  be a proxy-wrapper around the type that implements the interface
 * @author spb
 *
 * @param <R> Type of intermediate record (type input is split into) for parse
 */
public interface UploadParseTarget<R> extends
		PropertyContainerParseTarget<R> {
	/** Returns an uncommitted {@link ExpressionTargetContainer} to match the input container.
	 * This is either a new object/proxy or an existing object/proxy selected using the
	 * key properties.
	 * 
	 * @param value
	 * @return new or existing object 
	 * @throws AccountingParseException 
	 * 
	 */
	public ExpressionTargetContainer make(PropertyContainer value) throws AccountingParseException;
	
	/** extract global properties from post parameters.
	 * 
	 * @param params
	 * @return PropertyMap
	 */
	public PropertyMap getGlobals(Map<String,Object> params);

}