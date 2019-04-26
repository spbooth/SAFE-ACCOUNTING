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

import uk.ac.ed.epcc.webapp.model.data.Duration;

/**
 * Formats {@link Duration} object so that it is represented as an XML duration.
 * XML Durations may contain units in months and years. As their length of time
 * in hours, minutes and seconds is ambiguous, this formatter will never use
 * such units
 * 
 * @author jgreen4
 * 
 */


public class XMLDurationFormatter implements ValueFormatter<Duration> {

	/**
	 * Useful static instance of this object to be used when one doesn't want to
	 * generate lots of formatters when one will suffice
	 */
	public static final XMLDurationFormatter FORMATTER = new XMLDurationFormatter();

	private static final long MILLISECONDS_SCALE = 1;
	private static final long SECONDS_SCALE = MILLISECONDS_SCALE * 1000l;
	private static final long MINUTES_SCALE = SECONDS_SCALE * 60l;
	private static final long HOURS_SCALE = MINUTES_SCALE * 60l;

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
		long durationRemaining = duration.getMilliseconds();

		
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
		if (seconds > 0) {
			sb.append(seconds);
			if (milliseconds > 0)
				sb.append('.').append(milliseconds);

			sb.append('S');
		} else if (milliseconds > 0) {
			sb.append('.').append('0').append(milliseconds).append('S');
		}

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter#getType()
	 */
	public Class<Duration> getType() {
		return Duration.class;
	}

}