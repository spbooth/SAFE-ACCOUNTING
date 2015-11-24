// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Parses a string into a <code>Date</code>. There are many formats of a date
 * can be represented in. As a result, this parser must be given an appropriate
 * date <code>DateFormat</code> object to know how to parse a string into a
 * date. In particular situations where the same format is being used for a
 * larger more complicated parser, it may be more appropriate to subclass this
 * object and hard code the format into the class.
 * 
 * @author jgreen4
 * 
 */
@uk.ac.ed.epcc.webapp.Version("$Id: DateParser.java,v 1.12 2014/09/15 14:32:25 spb Exp $")

public class DateParser implements ValueParser<Date> {
	
	private static final SimpleDateFormat defaultFormat = new SimpleDateFormat(
	"yyyy-MM-dd");
	
	/**
	 * Simple date format that accepts dates in the default ISO 8601 format yyyy-MM-dd
	 */
	public static final DateParser PARSER = new DateParser(defaultFormat);
	
	/**
	 * The date format used
	 */
	private DateFormat format;

	public DateParser(DateFormat format) {
		this.format = format;
	}
	
	public DateParser() {
		this.format = defaultFormat;
	}

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
	public Date parse(String valueString) throws  ValueParseException {
		if (valueString == null)
			throw new ValueParseException("valueString cannot be null");
		
		valueString = valueString.trim();
		try {
			return this.format.parse(valueString.trim());
		} catch (ParseException e) {
			throw new ValueParseException("Bad date format",e);
		}
	}

	/**
	 * Takes a date and returns a string representation of it using the format
	 * this parser uses to convert strings into dates.
	 * 
	 * @param date
	 *          The date to be converted into a string
	 * @return <code>date</code> in string format
	 */
	public String format(Date date) {
		return this.format.format(date);
	}

}