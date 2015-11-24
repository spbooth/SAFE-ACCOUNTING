// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;

/**
 * Parser that parses a string into an Java <code>Date</code>. The format of the
 * string should follow the XML schema DateTime specification. A
 * <code>NullPointerException</code> is thrown if the string provided is
 * <code>null</code>.
 * 
 * @author jgreen4
 * 
 */
@uk.ac.ed.epcc.webapp.Version("$Id: XMLDateTimeParser.java,v 1.18 2014/09/15 14:32:26 spb Exp $")

public class XMLDateTimeParser implements ValueParser<Date> {
	/**
	 * Useful static instance of this object to be used when one doesn't want to
	 * generate lots of parsers when one will suffice
	 */
	public static final XMLDateTimeParser PARSER = new XMLDateTimeParser();

	/*
	 * No argument constructor of the superclass is automatically called
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Date> getType() {
		return Date.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.parsers.ValueParser#parse(java.lang.String)
	 */
	public Date parse(String valueString)
    throws IllegalArgumentException,
    NullPointerException
  {
    DatatypeFactory xmlDataTypeFactory;
	try {
		xmlDataTypeFactory = DatatypeFactory.newInstance();
	} catch (DatatypeConfigurationException e) {
		throw new ConsistencyError("Error getting DataTypeFactory",e);
	}
    XMLGregorianCalendar xmlCal = null;
    try {
    xmlCal =
      xmlDataTypeFactory.newXMLGregorianCalendar(valueString.trim());
    } catch (IllegalArgumentException e) {
    	throw new IllegalArgumentException(valueString);
    }

    return xmlCal.toGregorianCalendar().getTime();
  }

	public String format(Date value) {
		DatatypeFactory xmlDataTypeFactory;
		try {
			xmlDataTypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new ConsistencyError("Error getting DataTypeFactory",e);
			
		}
	    XMLGregorianCalendar xmlCal = null;
	        // force to Zulu format for OGF-ur
	    	GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	    	cal.setTime(value);
	    xmlCal =
	      xmlDataTypeFactory.newXMLGregorianCalendar(cal);
	    // try to supress fractional sweconds for NGS
	    // certainly safe if the milliseconds are zero.
	    if( cal.get(Calendar.MILLISECOND) == 0){
	    	xmlCal.setFractionalSecond(null);
	    }
	     return xmlCal.toXMLFormat();
	}
}