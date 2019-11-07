package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.model.data.Duration;

public class XMLDurationParserTest extends DurationParserTest {

	@Override
	public String[] getParsable() {
		return new String[] { "PT10H00M00S" , "PT0H5M0S"};
	}

	@Override
	public ValueParser<Duration> getValueParser() {
		return new XMLDurationParser();
	}

}
