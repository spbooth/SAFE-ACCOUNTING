// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.Description;

/** Generic Number parser. 
 * Attempts to parse to the intrinsic number classes in turn.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: NumberParser.java,v 1.8 2014/09/15 14:32:25 spb Exp $")
@Description("Parse a generic number")
public class NumberParser implements ValueParser<Number> {

	public Class<Number> getType() {
		return Number.class;
	}

	public Number parse(String valueString) throws ValueParseException {
		if( valueString == null){
			return null;
		}
		valueString=valueString.trim();
		try{
			return Integer.parseInt(valueString);
		}catch(NumberFormatException e){
						
		}
		try{
			return Long.parseLong(valueString);
		}catch(NumberFormatException e){
						
		}
		try{
			return Double.parseDouble(valueString);
		}catch(NumberFormatException e){
						
		}
		throw new ValueParseException("Cannot parse "+valueString+" as number");
		
	}

	public String format(Number value) {
		return value.toString();
	}

}