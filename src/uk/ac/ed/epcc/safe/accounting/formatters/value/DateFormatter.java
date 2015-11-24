// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Description;
@uk.ac.ed.epcc.webapp.Version("$Id: DateFormatter.java,v 1.6 2014/09/15 14:32:22 spb Exp $")
/** Formats a date. By default use ISO 8601 format as this is
 * what the value-parser will take by default
 * 
 * @author spb
 *
 */
@Description("Format a date")
public class DateFormatter implements DomFormatter<Date> {

	public static final SimpleDateFormat default_format = new SimpleDateFormat("yyyy-MM-dd");
	private final DateFormat fmt;
	
	public DateFormatter(DateFormat f){
		this.fmt=f;
	}
	public DateFormatter(){
		this(default_format);
	}
	
	public Class<Date> getTarget() {
		return Date.class;
	}

	
	public Node format(Document doc, Date date) throws Exception {
		if(date == null ){
			return null;
		}
		String result=fmt.format(date);
		
		return doc.createTextNode(result);
	}

}