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
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Description;

/** Formats a date. By default use ISO 8601 format as this is
 * what the value-parser will take by default
 * 
 * @author spb
 *
 */
@Description("Format a date")
public class DateFormatter implements DomFormatter<Date> {

	private static final String DEFAULT_FORMAT = "yyyy-MM-dd";
	private final DateFormat fmt;
	
	public DateFormatter(DateFormat f){
		this.fmt=f;
	}
	public DateFormatter(){
		this(new SimpleDateFormat(DEFAULT_FORMAT));
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