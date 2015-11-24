// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.text.SimpleDateFormat;

/**
 * Parses a ISO 8601 Date/time string into a <code>Date</code>. 
 */
@uk.ac.ed.epcc.webapp.Version("$Id: DateTimeParser.java,v 1.3 2014/09/15 14:32:25 spb Exp $")

public class DateTimeParser extends DateParser{
	
	private static final SimpleDateFormat defaultFormat = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss");
	
	/**
	 * Simple date format that accepts dates in the default ISO 8601 format yyyy-MM-dd
	 */
	public static final DateTimeParser PARSER = new DateTimeParser();
	
	public DateTimeParser(){
		super(defaultFormat);
	}
}