// Copyright - The University of Edinburgh 2011
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
@uk.ac.ed.epcc.webapp.Version("$Id: ValueParserFormat.java,v 1.9 2014/09/15 14:32:26 spb Exp $")

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