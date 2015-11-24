package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.forms.inputs.DateInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TimeStampInput;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
@Description("Start time of a TimePeriod")
public class StartDateFormatter<T extends TimePeriod> implements DomFormatter<T> {
	public static final SimpleDateFormat default_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Class<? super T> getTarget() {
		
		return TimePeriod.class;
	}

	public Node format(Document doc, T value) throws Exception {
		if(value == null){
			return null;
		}
		Date date=value.getStart();
		String result=default_format.format(date);
		
		return doc.createTextNode(result);
	}

}
