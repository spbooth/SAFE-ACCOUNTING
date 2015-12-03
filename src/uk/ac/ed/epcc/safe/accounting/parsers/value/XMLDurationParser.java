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

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.model.data.Duration;

/**
 * <p>
 * Parser that parses a string into a duration measured in seconds (represented
 * as a <code>Number</code>. The number may be a floating point number if the
 * time was measured to an accuracy greater than a second. The format of the
 * string should follow the XML schema Duration specification. A
 * <code>NullPointerException</code> is thrown if the string provided is
 * <code>null</code>.
 * </p>
 * <p>
 * Durations are harder to parse than most strings that represent one entity.
 * Durations can be specified using days, months and years. The length in
 * seconds of a month and year vary depending when the measurement was made. In
 * the same way, if a duration is measured by observing the time at the start
 * and end of the duration, the length of a day can vary if the measurement was
 * made across a day boundary when daylight saving changes were applied.
 * </p>
 * <p>
 * This parser takes two approaches to parsing a duration. If used in the same
 * way as other <code>ValueParser</code>s, this parser takes no arguments. In
 * this mode it can parse any duration that does not contain months or years.
 * Days are assumed to always be 24 hours long (i.e. daylight saving changes are
 * ignored). If months and years are present in a duration when a parser is in
 * this mode, the parse will fail. Alternatively, a date can be specified for
 * when the duration started. This can be done at construction time or after
 * using the appropriate setter method. All durations may then be parsed. In
 * this mode, daylight saving changes will be taken into account.
 * </p>
 * <p>
 * The parser uses the {@link DatatypeFactory} factory to generate Duration
 * objects to parse it's dates (regardless of which mode it is in). As such, has
 * the same restrictions as the factory when generating {@link Duration} objects
 * from a string. The parser also makes use of some of the <code>int</code>
 * returning methods in <code>Duration</code> and as such is susceptible to
 * integer overflow as stated in <code>Duration</code>'s documentation. This
 * should only happen for very large durations.
 * </p>
 * 
 * @author jgreen4
 * 
 */


public class XMLDurationParser implements ValueParser<Duration> {
	/**
	 * WARNING: this default static parser is constructed using the no-argument
	 * constructor of XMLDurationParser. As such it suffers the limitations of the
	 * parser in this state. See the documentation for this class for more
	 * information as to what they are.
	 */
	public static final XMLDurationParser PARSER = new XMLDurationParser();
	
	private static final long MILLISECONDS_SCALE = 1;
	private static final long SECONDS_SCALE = MILLISECONDS_SCALE * 1000l;
	private static final long MINUTES_SCALE = SECONDS_SCALE * 60l;
	private static final long HOURS_SCALE = MINUTES_SCALE * 60l;

	private Date start;

	public XMLDurationParser() {
		super();
	}

	public XMLDurationParser(Date durationStart) {
		super();
		this.setStartOfDuration(durationStart);
	}

	public XMLDurationParser(Calendar durationStart) {
		super();
		this.setStartOfDuration(durationStart);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Duration> getType() {
		return Duration.class;
	}

	public void setStartOfDuration(Date durationStart) {
		this.start = durationStart;
	}

	public void setStartOfDuration(Calendar durationStart) {
		this.start = durationStart.getTime();
	}

	/**
	 * <p>
	 * Parses an XML duration string and converts it into a duration in seconds.
	 * This operation can be greatly complicated if the duration features a number
	 * of days, months or years. In this situation, it is impossible to determine
	 * the number of seconds that have occurred in the duration without knowing
	 * when the duration started because the number of seconds in a day/month/year
	 * depends on which day/month/year was measured. This parser allows the start
	 * date of a duration to be set.
	 * </p>
	 * 
	 * <p>
	 * If a parse is attempted without the start date being set, this parser
	 * attempts to parse the duration anyway. If the duration contains months and
	 * years, the parse will fail. If the duration contains days, the parser
	 * assumes a day contains 24 hours. This is usually correct but can be wrong
	 * if duration is measured using the start and end time of an event when the
	 * event happens over a change in daylight saving changes (then, a day appears
	 * to have 23 or 25 hours in it when measuring duration as the difference
	 * between start and end datetimes).
	 * </p>
	 * 
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#parse(java.lang.String)
	 */
	public Duration parse(String valueString) throws IllegalArgumentException,
			NullPointerException {
		/*
		 * NOTE: this could have been implemented using a dummy date and parsing the
		 * duration using duration.getTimeInMillis(new Date()). However, the
		 * duration may then cross over a daylight saving time switch and be out by
		 * an hour. We avoid this by calculating the time explicitly and ignoring
		 * daylight savings
		 */

		javax.xml.datatype.Duration xmlDuration = this.parseToDuration(valueString);

		if (this.start != null) {
			double durationInMillis = xmlDuration.getTimeInMillis(this.start);
			return new Duration(durationInMillis / 1000.0);
		}

		/*
		 * If start date is null, we'll work out the duration manually. However, we
		 * can't do that if months and years are specified so we'll have to make
		 * sure they aren't first
		 */
		if (xmlDuration.getYears() != 0 || xmlDuration.getMonths() != 0)
			throw new IllegalArgumentException("Cannot parse duration " + xmlDuration
					+ " into seconds.  Start time not specified");

		double days = xmlDuration.getDays();
		double hours = xmlDuration.getHours();
		double mins = xmlDuration.getMinutes();
		double secs = 0;
		Number field = xmlDuration.getField(DatatypeConstants.SECONDS);
		if( field != null ){
			secs = field.doubleValue();
		}

		double durationInSeconds = secs + 60 * (mins + 60 * (hours + 24 * days));
		return new Duration(durationInSeconds);
	}

	/**
	 * Convenient method to parse a string into a <code>Duration</code> object
	 * instead of a Java <code>Date</code> object.
	 * 
	 * @param valueString
	 *          The string to parse
	 * @return The <code>Duration</code> object <code>valueString</code>
	 *         represents
	 * @throws IllegalArgumentException
	 *           If <code>valueString</code> cannot be parsed into a
	 *           <code>Duration</code> object
	 */
	public javax.xml.datatype.Duration parseToDuration(String valueString)
			throws IllegalArgumentException {
		try {
			return DatatypeFactory.newInstance().newDuration(
					valueString.trim());
		} catch (DatatypeConfigurationException e) {
			throw new ConsistencyError("Error getting DataTypeFactory",e);

		}
	}
// This works in XML terms but the OGF specification is tighter.
// than the XML one.
//	public String format(Duration value) {
//		javax.xml.datatype.Duration d;
//		try {
//			d = DatatypeFactory.newInstance().newDuration(value.getMilliseconds());
//		} catch (DatatypeConfigurationException e) {
//			throw new ConsistencyError("Error getting DataTypeFactory",e);
//		}
//		return d.toString();
//	}
	/**
	 * Formats the duration as an XML duration. The units used are hours, minutes,
	 * seconds and milliseconds. The duration uses as much of the largest unit as
	 * it can before moving down to the next biggest unit until the duration is
	 * completely represented. For example. Three hours, sixty minutes will always
	 * be represented as PT4H and never, PT3H60M. The formatted string will be as
	 * accurate as the duration given to it, but it may be more precise than the
	 * given duration.
	 * 
	 * @return the duration as an XML duration
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter#format(java.lang.Object)
	 */
	public String format(Duration duration) {
		
		if (duration.getMilliseconds() == -1) {
			return "";
		}

		long hours = 0;
		long minutes = 0;
		long seconds = 0;
		long milliseconds = 0;
		long total = duration.getMilliseconds();
		long durationRemaining=total;
		
		hours = durationRemaining / HOURS_SCALE;
		durationRemaining -= hours * HOURS_SCALE;
		if (durationRemaining > 0) {
			minutes = durationRemaining / MINUTES_SCALE;
			durationRemaining -= minutes * MINUTES_SCALE;
			if (durationRemaining > 0) {
				seconds = durationRemaining / SECONDS_SCALE;
				durationRemaining -= seconds * SECONDS_SCALE;
			}
		}
		milliseconds = durationRemaining;

		// Now we have the information, generate the string using it
		StringBuilder sb = new StringBuilder("PT");
		if (hours > 0)
			sb.append(hours).append('H');
		if (minutes > 0)
			sb.append(minutes).append('M');
		if( milliseconds == 0 ){
			if( seconds > 0 || total == 0 ){
				sb.append(seconds).append('S');
			}
		}else{
			double value = (milliseconds + seconds * SECONDS_SCALE)/1000.0;
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumIntegerDigits(1);
			nf.setMinimumFractionDigits(0);
			nf.setMaximumFractionDigits(3);
			sb.append(nf.format(value)).append('S');
		}
		return sb.toString();
	}
}