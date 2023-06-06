//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
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


public class DateParser implements ValueParser<Date> {
	
	
	/**
	 * The date format used
	 */
	private DateFormat format;

	public DateParser(DateFormat format) {
		this.format = format;
	}
	
	public DateParser() {
		this(new SimpleDateFormat("yyyy-MM-dd"));
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
		if( generateNull(valueString)) {
			return null;
		}
		try {
			return this.format.parse(valueString.trim());
		} catch (ParseException e) {
			throw new ValueParseException("Bad date format",e);
		}
	}

	protected boolean generateNull(String val) {
		return false;
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