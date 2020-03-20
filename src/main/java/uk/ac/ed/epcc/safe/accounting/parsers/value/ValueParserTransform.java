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

import uk.ac.ed.epcc.webapp.content.Transform;
/** A Table.Transform that uses a ValueParser to format the table contents.
 * 
 * @author spb
 *
 * @param <T>
 */


public class ValueParserTransform<T> implements Transform{
    private final ValueParser<T> parser;
    public ValueParserTransform(ValueParser<T> p){
    	parser=p;
    }
	@SuppressWarnings("unchecked")
	public Object convert(Object old) {
		if( old == null ){
			return null;
		}
		if( parser != null ){
			T val = parser.convert(old);
			if( val != null ) {
				return parser.format(val);
			}
		}
		return old;
	}

}