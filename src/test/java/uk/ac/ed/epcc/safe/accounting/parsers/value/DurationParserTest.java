package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.model.data.Duration;

public class DurationParserTest extends ValueParserAbstractTest<Duration> {

	@Override
	public Class<Duration> getTarget() {
		return Duration.class;
	}

	@Override
	public String[] getParsable() {
		return new String[] { "10:00:00" , "0:5:0" , "100000"};
	}

	@Override
	public Duration[] getData() {
		return new Duration[] { new Duration(500), new Duration(100,1L), new Duration(10,1L)} ;
	}

	@Override
	public String[] getErrors() {
		return new String[] { "boris", "999" , "9999999"};
	}


}
