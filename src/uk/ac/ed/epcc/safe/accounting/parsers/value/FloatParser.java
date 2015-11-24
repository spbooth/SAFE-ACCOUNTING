// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.Description;

@uk.ac.ed.epcc.webapp.Version("$Id: FloatParser.java,v 1.7 2014/09/15 14:32:25 spb Exp $")

@Description("Parse a float")
public class FloatParser implements ValueParser<Float> {

	public Class<Float> getType() {
		return Float.class;
	}

	public Float parse(String valueString) throws ValueParseException {
		if(valueString==null){
			return null;
		}
		try{
		return Float.parseFloat(valueString.trim());
		}catch(NumberFormatException e){
			throw new ValueParseException(e);
		}
	}

	public String format(Float value) {
		return Float.toString(value);
	}

}