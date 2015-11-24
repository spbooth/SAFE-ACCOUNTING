// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;


/**
 * <p>
 * A parser for identifying durations specified in a particular format. The
 * duration is converted into a number (a long) representing the number of
 * seconds of the duration.
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
@uk.ac.ed.epcc.webapp.Version("$Id: SecondDurationParser.java,v 1.2 2014/09/15 14:32:26 spb Exp $")

public class SecondDurationParser implements ValueParser<Integer> {

	public static final SecondDurationParser PARSER = new SecondDurationParser();

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#getType()
	 */
	public Class<Integer> getType() {
		return Integer.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser#parse(java.lang
	 * .String)
	 */
	public Integer parse(String valueString) throws IllegalArgumentException,
			NullPointerException {

		String timeElems[] = valueString.trim().split(":");
		if (timeElems.length != 3) {
			if (valueString.length() != 6)
				throw new IllegalArgumentException("Bad duration format: '"
						+ valueString + "'");

			timeElems = new String[3];
			timeElems[0] = valueString.substring(0, 2);
			timeElems[1] = valueString.substring(2, 4);
			timeElems[2] = valueString.substring(4, 6);
		}

		int hours = Integer.parseInt(timeElems[0]);
		int minutes = Integer.parseInt(timeElems[1]);
		int seconds = Integer.parseInt(timeElems[2]);

		return new Integer((hours * 3600) + (minutes * 60) + (seconds));
	}

	public String format(Integer value) {
		long time = value;
		long seconds = time % 60;
		time = time / 60;
		long minutes = time % 60;
		long hours = time / 60;
		return ""+hours+":"+minutes+":"+seconds;
	}
}