// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.reports;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.safe.accounting.formatters.value.DomFormatter;
import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;

/** DomFormatter that formats a Period as the corresponding XML fragment
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: PeriodFormatter.java,v 1.5 2014/09/23 15:35:45 spb Exp $")
@Description("Formats a Period as the corresponding XML element")
public class PeriodFormatter implements DomFormatter<TimePeriod> {

	public Class<TimePeriod> getTarget() {
		return TimePeriod.class;
	}

	public Node format(Document doc, TimePeriod value) throws Exception {
		return PeriodExtension.format(doc, value);
		
	}

}