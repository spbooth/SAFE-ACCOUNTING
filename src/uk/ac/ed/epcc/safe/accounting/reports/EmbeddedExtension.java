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
package uk.ac.ed.epcc.safe.accounting.reports;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import uk.ac.ed.epcc.webapp.AppContext;
/** Extracts and stores fragments of the generated XML.
 * 
 *  This is for when report generation is embedded in some other 
 *  process (for example automatic reports set by email) that
 *  needs to take configuration parameters from the template/generated-XML.
 *  
 * 
 */
public class EmbeddedExtension extends ReportExtension {

	Map<String,Object> fragments = new HashMap<>();
	public EmbeddedExtension(AppContext conn, NumberFormat nf)
			throws ParserConfigurationException {
		super(conn, nf);
	}
	
	public String addFragment(String key, Object data){
		fragments.put(key, data);
		return "";
	}
	
	public Object getFragment(String key){
		return fragments.get(key);
	}

}