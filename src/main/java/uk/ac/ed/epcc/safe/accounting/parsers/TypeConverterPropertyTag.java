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
package uk.ac.ed.epcc.safe.accounting.parsers;

import uk.ac.ed.epcc.safe.accounting.expr.TypeConverterPropExpression;
import uk.ac.ed.epcc.safe.accounting.parsers.value.TypeConverterValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserProvider;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.convert.TypeConverter;
/** PropertyTag for properties generated by a TypeConverter with an
 * underlying String representation. This generates its own {@link ValueParser}
 * based on this String representation.
 * 
 * Normally these are installed as derived properties pointing to a {@link TypeConverterPropExpression} on a standard
 * String {@link PropertyTag} representing the bare field.
 * 
 
 * @author spb
 *
 * @param <T> produced type
 */
public class TypeConverterPropertyTag<T> extends PropertyTag<T> implements
		ValueParserProvider<T> {

	private final TypeConverter<T, String> converter;
	private final Class<T> clazz;
	public TypeConverterPropertyTag(PropertyRegistry registry, String name,
			Class<T> clazz,TypeConverter<T, String> converter, String description) {
		super(registry, name, clazz, description);
		this.clazz=clazz;
		this.converter=converter;
	}

	public TypeConverterPropertyTag(PropertyRegistry registry, String name,
			Class<T> clazz,TypeConverter<T, String> converter) {
		super(registry, name, clazz);
		this.clazz=clazz;
		this.converter=converter;
	}

	public ValueParser<T> getValueParser(AppContext c) {
		return new TypeConverterValueParser<>(clazz, converter);
	}
	public RecordSelector makeSelector(T val){
		return new SelectClause<>(this,val);
	}

}