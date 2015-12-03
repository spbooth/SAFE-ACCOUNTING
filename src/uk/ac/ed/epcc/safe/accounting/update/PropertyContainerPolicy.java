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
package uk.ac.ed.epcc.safe.accounting.update;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;


/** A policy object.
 * 
 * These take part in the parse process but rather than handle the input directly they
 * apply transformations on the properties generated earlier in the process.
 * 
 * @author spb
 *
 */
public interface PropertyContainerPolicy extends PropertyContainerUpdater {

	/**
	 * Generate additional properties based on properties from the basic parse
	 * and previously applied Policies
	 * 
	 * @param rec
	 *            PropertyMap being modified
	 * @throws AccountingParseException
	 */
	public void parse(PropertyMap rec) throws AccountingParseException;
	
	
}