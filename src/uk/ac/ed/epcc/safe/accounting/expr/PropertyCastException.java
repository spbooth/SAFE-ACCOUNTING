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
package uk.ac.ed.epcc.safe.accounting.expr;
/** Exception thrown when the target type for a prop-expression is inappropriate for its context+
 * 
 * @author spb
 *
 */


public class PropertyCastException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3458183443198227797L;

	public PropertyCastException() {
		
	}

	public PropertyCastException(String message) {
		super(message);
		
	}

	public PropertyCastException(Throwable cause) {
		super(cause);
		
	}

	public PropertyCastException(String message, Throwable cause) {
		super(message, cause);
		
	}

}