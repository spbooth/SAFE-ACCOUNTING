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
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.security.Principal;

import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;

/**
 * A simple default formatting object. This formatter will format any type of
 * object. All it does is call the object's <code>toString</code> method.
 * 
 * @author jgreen4
 * 
 */


public class DefaultFormatter implements ValueFormatter<Object> {

	/**
	 * Useful static instance of this object to be used when one doesn't want to
	 * generate lots of formatters when one will suffice
	 */
	public static final DefaultFormatter FORMATTER = new DefaultFormatter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter#format(java
	 * .lang.Object)
	 */
	public String format(Object value) {
			
			if( value == null ){
				return null;
			}
			if( value instanceof Principal){
				return ((Principal)value).getName();
			}
			if( value instanceof Indexed){
				return Integer.toString(((Indexed)value).getID());
			}
			return value.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter#getType()
	 */
	public Class<Object> getType() {
		return Object.class;
	}

}