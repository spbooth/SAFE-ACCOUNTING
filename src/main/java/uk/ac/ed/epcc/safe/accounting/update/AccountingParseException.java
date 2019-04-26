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
/** An exception that indicates a fatal error in the parse of an AccountingRecord
 * If such an exception is thrown the parse will move onto the next record in the update.
 * 
 * Care should be taken to ensure that if the same error occurs in multiple update records
 * the same exception message should be returned. This will allow
 * the parse to return the message once with a count value and keep the 
 * amount of error reporting to a minimum
 * 
 * @author spb
 *
 */


public class AccountingParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3405399311516702174L;

	public AccountingParseException() {
		super();
	}

	public AccountingParseException(String arg0) {
		super(arg0);
	}

	public AccountingParseException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AccountingParseException(Throwable arg0) {
		super(arg0);
	}

}