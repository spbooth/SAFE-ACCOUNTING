// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.Description;

@uk.ac.ed.epcc.webapp.Version("$Id: BooleanParser.java,v 1.13 2014/09/15 14:32:25 spb Exp $")

@Description("Parse a boolean")
public class BooleanParser implements ValueParser<Boolean> {
	/**
	 * Useful static instance of this object to be used when one doesn't want to
	 * generate lots of parsers when one will suffice
	 */
	public static final BooleanParser PARSER = new BooleanParser();

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Boolean> getType() {
		return Boolean.class;
	}

	/**
	 * Determines the boolean value of the string.
	 * 
	 * @return <code>true</code> if <code>valueString</code> is equal to the
	 *         string <em>true</em>. Returns <code>false</code> if
	 *         <code>valueString</code> is equal to the string <em>false</em>.
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#parse(java.lang
	 *      .String)
	 */
	public Boolean parse(String valueString) throws IllegalArgumentException,
			NullPointerException {
		if(valueString == null)
			throw new IllegalArgumentException("null boolean value not allowed");
		
		return Boolean.parseBoolean(valueString.trim());
		
	}

	public String format(Boolean value) {
		return value.toString();
	}
}