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

@Description("Formats a period as text giving an inclusive date range")
public class TextPeriodFormatter implements DomFormatter<TimePeriod> {

	DateFormat df = new SimpleDateFormat("dd MMMMMMM yyyy");
	
	public Class<TimePeriod> getTarget() {
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