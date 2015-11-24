// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.util.Date;


/**
 * Parses a string into a <code>Date</code>. The string should be the character
 * representation of a unix timestamp. In other words, the string should be
 * parsable to an integer (well, technically a long) and that number should
 * represent a date by the number of seconds that have passed since January 1st
 * 1970.
 * 
 * @author jgreen4
 */
@uk.ac.ed.epcc.webapp.Version("$Id: TimestampParser.java,v 1.6 2014/09/15 14:32:26 spb Exp $")

public class TimestampParser implements ValueParser<Date>
{
	
  /**
   * Useful static instance of this object to be used when one doesn't want to
   * generate lots of parsers when one will suffice
   */
  public static final TimestampParser PARSER = new TimestampParser();

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Date> getType() {
		return Date.class;
	}
  
  /*
   * (non-Javadoc)
   * @see
   * uk.ac.ed.epcc.safe.accounting.parsers.ValueParser#parse(java.lang.String)
   */
  public Date parse(String valueString)
    throws IllegalArgumentException,
    NullPointerException
  {
    if(valueString == null)
      throw new NullPointerException("valueString cannot be null");

    double stamp = Double.parseDouble(valueString.trim());
    stamp = stamp * 1000L;
    return new Date((long) stamp);
  }
  
  /**
   * Takes a date and returns a string representation of it using the format this
   * parser uses to convert strings into dates.  In this case, the format will be the unix timestamp:
   * The number of seconds since January 1st 1970
   * 
   * @param date The date to be converted into a string
   * @return <code>date</code> in string format
   */
  public String format(Date date)
  {
    long secondTime = date.getTime()/1000L;
    return Long.toString(secondTime);
  }
}