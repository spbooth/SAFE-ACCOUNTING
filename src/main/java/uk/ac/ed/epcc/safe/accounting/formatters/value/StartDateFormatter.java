//| Copyright - The University of Edinburgh 2015                            |
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

	public Class<T> getTarget() {
		
		return (Class<T>) TimePeriod.class;
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