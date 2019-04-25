//| Copyright - The University of Edinburgh 2015                            |
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

import java.util.Iterator;

import uk.ac.ed.epcc.webapp.model.data.convert.EnumeratingTypeConverter;
import uk.ac.ed.epcc.webapp.model.data.convert.TypeConverter;

/** ValueParser that wraps a {@link TypeConverter}
 * 
 * @author spb
 *
 * @param <T>
 */
public class TypeConverterValueParser<T> implements ValueParser<T> {

	private final TypeConverter<T, String> converter;
	private final Class<T> type;
	
	public TypeConverterValueParser(Class<T> type, TypeConverter<T, String> converter){
		this.type=type;
		this.converter=converter;
	}
	public Class<T> getType() {
		return type;
	}

	public T parse(String valueString) throws ValueParseException {
		if( valueString == null || valueString.trim().length()==0){
			throw new ValueParseException("No value");
		}
		T value = converter.find(valueString);
		if(value == null ){
			if( converter instanceof EnumeratingTypeConverter){
				// Try to look for values via string type
				EnumeratingTypeConverter<T,String> et = (EnumeratingTypeConverter<T, String>) converter;
				for(Iterator<T> it= et.getValues(); it.hasNext();){
					T item = it.next();
					if( item.toString().equals(valueString)){
						return item;
					}
				}
			}
			throw new ValueParseException(valueString);
		}
		return value;
	}

	public String format(T value) {
		return converter.getIndex(value);
	}

}