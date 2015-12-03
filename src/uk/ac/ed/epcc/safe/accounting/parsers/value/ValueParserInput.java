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
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.forms.exceptions.ParseException;
import uk.ac.ed.epcc.webapp.forms.inputs.ParseAbstractInput;
/** Adapter to convert ValueParser to Input
 * 
 * @author spb
 *
 * @param <T>
 */


public class ValueParserInput<T> extends ParseAbstractInput<T>  {

	private ValueParser<T> parser;
	public ValueParserInput(ValueParser<T> parser){
		this.parser=parser;
	}
	
	public void parse(String v) throws ParseException {
		if(v != null && v.length() > 0){
			try {
				setValue(parser.parse(v));
			} catch (Exception e) {
				throw new ParseException("Bad format", e);
			}
		}else{
			setValue(null);
		}
	}

	public String getString(T val){
		return parser.format(val);
	}
}