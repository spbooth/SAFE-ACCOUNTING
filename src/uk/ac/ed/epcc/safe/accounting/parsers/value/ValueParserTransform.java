// Copyright - The University of Edinburgh 2011
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
@uk.ac.ed.epcc.webapp.Version("$Id: ValueParserTransform.java,v 1.8 2014/09/26 16:14:58 spb Exp $")

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
		if( parser != null && parser.getType().isAssignableFrom(old.getClass())){
			return parser.format((T)old);
		}
		return old;
	}

}