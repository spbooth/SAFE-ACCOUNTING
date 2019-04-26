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
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.Description;



@Description("Parse a boolean")
public class BooleanParser implements ValueParser<Boolean> {
	/**
	 * Useful static instance of this object to be used when one doesn't want to
	 * generate lots of parsers when one will suffice
	 */
	public static final BooleanParser PARSER = new BooleanParser();

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Boolean> getType() {
		return Boolean.class;
	}

	/**
	 * Determines the boolean value of the string.
	 * 
	 * @return <code>true</code> if <code>valueString</code> is equal to the
	 *         string <em>true</em>. Returns <code>false</code> if
	 *         <code>valueString</code> is equal to the string <em>false</em>.
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#parse(java.lang
	 *      .String)
	 */
	public Boolean parse(String valueString) throws IllegalArgumentException,
			NullPointerException {
		if(valueString == null)
			throw new IllegalArgumentException("null boolean value not allowed");
		
		return Boolean.parseBoolean(valueString.trim());
		
	}

	public String format(Boolean value) {
		return value.toString();
	}
}