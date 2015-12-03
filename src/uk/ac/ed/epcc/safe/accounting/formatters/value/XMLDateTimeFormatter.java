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
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;

/**
 * Formats a {@link Date} object so that it is represented as an XML dataTime.
 * 
 * @author jgreen4
 * 
 */

@Description("Formats a Date object so that it is represented as an XML dataTime.")
public class XMLDateTimeFormatter implements ValueFormatter<Date> {

	/**
	 * Useful static instance of this object to be used when one doesn't want to
	 * generate lots of formatters when one will suffice
	 */
	public static final XMLDateTimeFormatter FORMATTER = new XMLDateTimeFormatter();

	/**
	 * Formats the date using Java's {@link Calendar} to extract years, months etc
	 * from a date object. An {@link XMLGregorianCalendar} is packed with this
	 * information and it's formatting method is used to generate the output
	 * 
	 * @return The date as an XML dateTime 
	 * 
	 * @see Calendar
	 * @see XMLGregorianCalendar#toXMLFormat()
	 * @see uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter#format(java.lang.Object)
	 */
	public String format(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		XMLGregorianCalendar xmlCal;
		try {
			xmlCal = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar();
		} catch (DatatypeConfigurationException e) {
			throw new ConsistencyError("Error getting DataTypeFactory",e);
		}

		BigDecimal fractionalSecond;
		fractionalSecond = BigDecimal.valueOf(cal.get(Calendar.MILLISECOND), 3);

		xmlCal.setYear(cal.get(Calendar.YEAR));
		xmlCal.setMonth(cal.get(Calendar.MONTH) + 1); // Calendar starts
		// counting months
		// from 0
		xmlCal.setDay(cal.get(Calendar.DAY_OF_MONTH));
		xmlCal.setHour(cal.get(Calendar.HOUR_OF_DAY));
		xmlCal.setMinute(cal.get(Calendar.MINUTE));
		xmlCal.setSecond(cal.get(Calendar.SECOND));
		xmlCal.setFractionalSecond(fractionalSecond);
		
		/* Time zone implementations are horribly mis-aligned between Calendar and
		 * XMLGregorianCalendar.  TimeZone returns an offset in milliseconds, 
		 * XMLGregorianCalendar wants the offset in minutes.
		 */
		TimeZone timeZone = cal.getTimeZone();
		long offsetMillis = timeZone.getOffset(new Date().getTime());
		int offsetMins = (int)(offsetMillis/60000);
		xmlCal.setTimezone(offsetMins);

		return xmlCal.toXMLFormat();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter#getType()
	 */
	public Class<Date> getType() {
		return Date.class;
	}

}