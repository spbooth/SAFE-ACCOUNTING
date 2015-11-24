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
		super(registry, name, converter.getTarget(), description);
		this.clazz=clazz;
		this.converter=converter;
	}

	public TypeConverterPropertyTag(PropertyRegistry registry, String name,
			Class<T> clazz,TypeConverter<T, String> converter) {
		super(registry, name, converter.getTarget());
		this.clazz=clazz;
		this.converter=converter;
	}

	public ValueParser<T> getValueParser(AppContext c) {
		return new TypeConverterValueParser<T>(clazz, converter);
	}
	public RecordSelector makeSelector(T val){
		return new SelectClause<T>(this,val);
	}

}
