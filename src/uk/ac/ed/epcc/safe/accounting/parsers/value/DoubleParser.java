// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.Description;

@uk.ac.ed.epcc.webapp.Version("$Id: DoubleParser.java,v 1.11 2014/09/15 14:32:25 spb Exp $")

@Description("Parse a double value")
public class DoubleParser implements ValueParser<Double> {

	public Class<Double> getType() {
		return Double.class;
	}

	public Double parse(String valueString) throws ValueParseException {
		if(valueString==null){
			return null;
		}
		try{
		return Double.parseDouble(valueString.trim());
		}catch(NumberFormatException e){
			throw new ValueParseException(e);
		}
	}

	public String format(Double value) {
		return Double.toString(value);
	}

}