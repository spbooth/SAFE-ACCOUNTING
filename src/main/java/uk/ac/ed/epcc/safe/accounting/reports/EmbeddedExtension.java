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

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
    public static final String EMBEDDED_LOC="http://safe.epcc.ed.ac.uk/embedded";
	Map<String,Object> fragments = new HashMap<>();
	public EmbeddedExtension(AppContext conn, ReportType type)
			throws ParserConfigurationException {
		super(conn, type);
	}
	
	public String addFragment(String key, Object data){
		fragments.put(key, data);
		return "";
	}
	
	public Object getFragment(String key){
		return fragments.get(key);
	}

	@Override
	public boolean wantReplace(Element e) {
		return EMBEDDED_LOC.equals(e.getNamespaceURI());
	}

	@Override
	public Node replace(Element e) {
		String name = e.getLocalName();
		switch(name) {
		case "Define": addFragment(e.getAttribute("name"), e); return null;
		}
		return null;
	}

}