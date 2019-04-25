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

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
/** A {@link Format} that delegates to a ValueParser
 * 
 * As ValueParser only parses full strings the parseObject method can also only match full strings
 * @author spb
 * @param <T> target type
 *
 */


public class ValueParserFormat<T> extends Format {

	private final ValueParser<T> parser;
	public ValueParserFormat(ValueParser<T> parser){
		this.parser=parser;
		assert(parser!=null);
		if( parser.getType() == null ){
			throw new ConsistencyError("null type from "+parser.getClass().getCanonicalName());
		}
		assert(parser.getType() != null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo,
			FieldPosition pos) {
		Class<T> type = parser.getType();
		if( obj != null && ! type.isAssignableFrom(obj.getClass())){
			throw new IllegalArgumentException("Incompatible types in ValueParserFormat");
		}
		return toAppendTo.append(parser.format((T) obj));
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		try {
			String substring = source.substring(pos.getIndex());
			Object res = parser.parse(substring);
			pos.setIndex(pos.getIndex()+substring.length());
			return res;
		} catch (ValueParseException e) {
			pos.setErrorIndex(pos.getErrorIndex());
			return null;
		}
	}

}