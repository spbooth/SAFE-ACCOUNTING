// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.text.NumberFormat;

import uk.ac.ed.epcc.webapp.Description;
@uk.ac.ed.epcc.webapp.Version("$Id: DetailedPercentFormatter.java,v 1.2 2014/09/15 14:32:23 spb Exp $")

@Description("Format number as a percentage")
public class DetailedPercentFormatter implements ValueFormatter<Number> {

	NumberFormat nf;
	
	public DetailedPercentFormatter(){
		nf= NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(2);
	}
	
	public Class<Number> getType() {
		return Number.class;
	}

	public String format(Number object) {
		return nf.format(object.doubleValue());
	}

}