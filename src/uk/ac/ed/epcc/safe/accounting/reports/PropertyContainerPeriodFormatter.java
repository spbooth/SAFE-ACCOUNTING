//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
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