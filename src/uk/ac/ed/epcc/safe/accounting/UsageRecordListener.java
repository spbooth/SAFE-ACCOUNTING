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

import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;

/** Interface for factory classes that need to see 
 * record insertion/deletion for example aggregate tables or
 * budget managers. These methods are invoked by
 * a policy on the accounting table. Unlike a policy these do not
 * modify the behaviour/state of the usage record being parsed.
 * @author spb
 *
 */
public interface UsageRecordListener {
	/** Start a batch parse. This allocates any temporary state
	   */
	public void startListenerParse();
	/** End a batch parse this de-allocates any temporary storage and
	   * and perform any final operations.
	   * @return Error string if any
	   * 
	   *
	   */
	public String endListenerParse();
	
	/**
	 * once record has been committed apply any side effects such as budget
	 * charging.
	 * 
	 * @param props Full set of properties from the parse stage
	 * @param rec   The actual committed record
	 * @throws Exception
	 */
	public void postCreate(PropertyContainer props, UsageRecord rec)
			throws Exception;

	/**
	 * perform any operations required prior to an existing record being
	 * deleted, such as budget refunds
	 * 
	 * @param rec
	 * @throws Exception
	 */
	public void preDelete(UsageRecord rec) throws Exception;
   
}