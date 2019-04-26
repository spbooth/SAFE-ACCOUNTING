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

/** Generic Number parser. 
 * Attempts to parse to the intrinsic number classes in turn.
 * 
 * @author spb
 *
 */

@Description("Parse a generic number")
public class NumberParser implements ValueParser<Number> {

	public Class<Number> getType() {
		return Number.class;
	}

	public Number parse(String valueString) throws ValueParseException {
		if( valueString == null){
			return null;
		}
		valueString=valueString.trim();
		try{
			return Integer.parseInt(valueString);
		}catch(NumberFormatException e){
						
		}
		try{
			return Long.parseLong(valueString);
		}catch(NumberFormatException e){
						
		}
		try{
			return Double.parseDouble(valueString);
		}catch(NumberFormatException e){
						
		}
		throw new ValueParseException("Cannot parse "+valueString+" as number");
		
	}

	public String format(Number value) {
		return value.toString();
	}

}