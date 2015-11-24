// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
/** Formats a period a an <em>inclusive</em> date range
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: TextPeriodFormatter.java,v 1.5 2014/09/15 14:32:23 spb Exp $")
@Description("Formats a period as text giving an inclusive date range")
public class TextPeriodFormatter implements DomFormatter<TimePeriod> {

	DateFormat df = new SimpleDateFormat("dd MMMMMMM yyyy");
	
	public Class<? super TimePeriod> getTarget() {
		return TimePeriod.class;
	}

	




	
	public Node format(Document doc, TimePeriod tp) throws Exception {
		Date start = tp.getStart();
		Calendar c = Calendar.getInstance();
		c.setTime(tp.getEnd());
		c.add(Calendar.DAY_OF_YEAR, -1);
		return doc.createTextNode(df.format(start)+" to "+df.format(c.getTime()));
	}

}