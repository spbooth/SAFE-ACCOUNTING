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
