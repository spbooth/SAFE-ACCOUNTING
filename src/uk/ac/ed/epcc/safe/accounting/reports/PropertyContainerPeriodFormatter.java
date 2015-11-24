// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.safe.accounting.formatters.value.DomFormatter;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.time.SplitPeriod;
/** Class to derive a period from the time range of  PropertyContainer
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyContainerPeriodFormatter.java,v 1.5 2015/03/11 10:16:47 spb Exp $")
@Description("Generate a period element from the StartedTimestamp and CompletedTimestamp of the target.")
public class PropertyContainerPeriodFormatter implements DomFormatter<PropertyContainer>{

	public Class<? super PropertyContainer> getTarget() {
		return PropertyContainer.class;
	}

	public Node format(Document doc, PropertyContainer value) throws Exception {
		return PeriodExtension.format(doc, getPeriod(value));
	}

	public SplitPeriod getPeriod(PropertyContainer o) throws InvalidExpressionException{
		Date start = o.getProperty(StandardProperties.STARTED_PROP);
		Date end = o.getProperty(StandardProperties.ENDED_PROP);
		return SplitPeriod.getInstance(start, end);
	}
}