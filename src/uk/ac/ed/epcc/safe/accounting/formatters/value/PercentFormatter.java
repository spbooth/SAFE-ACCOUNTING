// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.text.NumberFormat;

import uk.ac.ed.epcc.webapp.Description;
@uk.ac.ed.epcc.webapp.Version("$Id: PercentFormatter.java,v 1.6 2014/09/15 14:32:23 spb Exp $")

@Description("Format number as a percentage")
public class PercentFormatter implements ValueFormatter<Number> {

	NumberFormat nf = NumberFormat.getPercentInstance();
	
	public Class<Number> getType() {
		return Number.class;
	}

	public String format(Number object) {
		return nf.format(object.doubleValue());
	}

}