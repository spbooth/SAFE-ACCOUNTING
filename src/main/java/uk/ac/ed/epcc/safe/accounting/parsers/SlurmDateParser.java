package uk.ac.ed.epcc.safe.accounting.parsers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import uk.ac.ed.epcc.safe.accounting.parsers.value.DateParser;

public class SlurmDateParser extends DateParser {

	public SlurmDateParser() {
		super(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
	}
	@Override
	protected boolean generateNull(String val) {
		return val.isEmpty() || val.equals("None");
	}
}
