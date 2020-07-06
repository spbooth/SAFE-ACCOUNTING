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



@Description("Parse a double value")
public class DoubleParser implements ValueParser<Double> {

	public static DoubleParser PARSER = new DoubleParser();
	public Class<Double> getType() {
		return Double.class;
	}

	public Double parse(String valueString) throws ValueParseException {
		if(valueString==null){
			return null;
		}
		try{
		return Double.parseDouble(valueString.trim());
		}catch(NumberFormatException e){
			throw new ValueParseException(e);
		}
	}

	public String format(Double value) {
		return Double.toString(value);
	}

}