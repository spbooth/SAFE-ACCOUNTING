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


/** Indicates a record that should be ignored without warning.
 * 
 * This can be thrown by a policy to abort the parse.
 * Parses can return false to abort a parse but they can also throw
 * {@link SkipRecord} to provide an informational message about why the record was skipped.
 * 
 * @author spb
 *
 */


public class SkipRecord extends AccountingParseException {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1983746511919729616L;

public SkipRecord(String reason){
	  super(reason);
  }
}