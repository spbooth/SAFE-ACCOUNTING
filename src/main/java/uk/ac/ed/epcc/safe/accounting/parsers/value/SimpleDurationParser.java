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

import uk.ac.ed.epcc.webapp.model.data.Duration;

/**
 * <p>
 * A parser for identifying durations specified in a particular format. The
 * duration is converted into a {@link Duration} the numerical value of which will be at the default
 * resolution (milliseconds)
 * </p>
 * <p>
 * The format must be either <em>HHmmss</em> or <em>HH:mm:ss</em> where
 * <em>H</em> is the number of hours, <em>m</em> is the number of minutes and
 * <em>s</em> is the number of seconds. If the format contains colons, the hour,
 * minute and second fields can contain values with more or less than two
 * digits. For example these formats are legal:
 * </p>
 * <blockquote> <b>000001</b> - one second<br/>
 * <b>100101</b> - ten hours one minute one second<br/>
 * <b>10:01:01</b> - ten hours one minute one second<br/>
 * <b>100:5:16</b> - one hundred hours five minutes sixteen seconds<br/>
 * <b>120:05:21</b> - one hundred and twenty hours five minutes twenty one
 * seconds<br/>
 * </blockquote>
 * <p>
 * However, if colons aren't present, the string must be exactly six characters
 * long with the first two characters representing hours, the second two
 * representing minutes and the third two representing seconds.
 * </p>
 * 
 * 
 * @author jgreen4
 * 
 */


public class SimpleDurationParser implements ValueParser<Duration> {

	public static final SimpleDurationParser PARSER = new SimpleDurationParser();

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Duration> getType() {
		return Duration.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#parse(java.lang
	 * .String)
	 */
	public Duration parse(String valueString) throws ValueParseException {
		 if(valueString == null)
		      throw new NullPointerException("valueString cannot be null");
		String timeElems[] = valueString.trim().split(":");
		if (timeElems.length != 3) {
			if (valueString.length() != 6)
				throw new ValueParseException("Bad duration format: '"
						+ valueString + "'");

			timeElems = new String[3];
			timeElems[0] = valueString.substring(0, 2);
			timeElems[1] = valueString.substring(2, 4);
			timeElems[2] = valueString.substring(4, 6);
		}

		long hours = Long.parseLong(timeElems[0]);
		long minutes = Long.parseLong(timeElems[1]);
		
		if( timeElems[2].contains(".")) {
			// handle fractional second
			double d = Double.parseDouble(timeElems[2]);
			d = d * 1000.0;
			long millis = (hours *3600000L) + (minutes * 60000L) + ((long)d);
			return new Duration(millis,1L);
		}
		
		long seconds = Long.parseLong(timeElems[2]);
		return new Duration((hours * 3600) + (minutes * 60) + (seconds));
	}

	public String format(Duration value) {
		long millis = value.getMilliseconds();
		
		if( millis % 1000L == 0L) {
			long time = value.getSeconds();
			long seconds = time % 60;
			time = time / 60;
			long minutes = time % 60;
			long hours = time / 60;
			return ""+hours+":"+minutes+":"+seconds;
		}
		long frac = millis % 1000L;
		millis = millis / 1000L;
		long seconds = millis % 60L;
		millis = millis / 60L;
		long minutes = millis % 60L;
		long hours = millis / 60L;
		return ""+hours+":"+minutes+":"+seconds+"."+String.format("%03d", frac);
	}

	@Override
	public Duration convert(Object in) {
		Duration v = ValueParser.super.convert(in);
		if( v != null || in == null  ) {
			return v;
		}
		// format any non Duration numbers as milliseconds
		if( in instanceof Number) {
			return new Duration((Number)in,1L);
		}
		return null;
	}
}