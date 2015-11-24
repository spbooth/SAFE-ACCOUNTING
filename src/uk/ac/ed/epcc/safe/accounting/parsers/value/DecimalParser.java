// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.math.BigDecimal;

import uk.ac.ed.epcc.webapp.Description;

/**
 * Parser that parses a string into an integer {@link Number}.  A
 * <code>NullPointerException</code> is thrown if the string provided is
 * <code>null</code>.
 * 
 * @author jgreen4
 * 
 */
@uk.ac.ed.epcc.webapp.Version("$Id: DecimalParser.java,v 1.8 2014/09/15 14:32:25 spb Exp $")
@Description("Parse a decimal value to a number")
public class DecimalParser implements ValueParser<Number>
{
  /**
   * Useful static instance of this object to be used when one doesn't want to
   * generate lots of parsers when one will suffice
   */
  public static final DecimalParser PARSER = new DecimalParser();

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Number> getType() {
		return Number.class;
	}
  
  /*
   * (non-Javadoc)
   * @see
   * uk.ac.ed.epcc.safe.accounting.parsers.ValueParser#parse(java.lang.String)
   */
  public Number parse(String valueString)
    throws IllegalArgumentException,
    NullPointerException
  {
    if(valueString == null)
      throw new NullPointerException("valueString cannot be null");

    valueString=valueString.trim();
    if(valueString.length() < 307) {
    	return Double.valueOf(valueString);
    } else {
    	return new BigDecimal(valueString);
    }
  }

public String format(Number value) {
	return value.toString();
}

}