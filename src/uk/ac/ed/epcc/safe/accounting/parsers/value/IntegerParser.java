// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.Description;



/**
 * Parser that parses a string into an <code>Integer</code> A
 * <code>NullPointerException</code> is thrown if the string provided is
 * <code>null</code>.
 * 
 * @author jgreen4
 * 
 */
@uk.ac.ed.epcc.webapp.Version("$Id: IntegerParser.java,v 1.10 2014/09/15 14:32:25 spb Exp $")
@Description("Parse an Integer")
public class IntegerParser implements ValueParser<Integer>
{
  /**
   * Useful static instance of this object to be used when one doesn't want to
   * generate lots of parsers when one will suffice
   */
  public static final IntegerParser PARSER = new IntegerParser();

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Integer> getType() {
		return Integer.class;
	}
  
  /*
   * (non-Javadoc)
   * @see
   * uk.ac.ed.epcc.safe.accounting.parsers.ValueParser#parse(java.lang.String)
   */
  public Integer parse(String valueString)
    throws IllegalArgumentException,
    NullPointerException
  {
    if(valueString == null)
      throw new NullPointerException("valueString cannot be null");

  
    return Integer.valueOf(valueString.trim());
 
  }

public String format(Integer value) {
	return Integer.toString(value);
}

}