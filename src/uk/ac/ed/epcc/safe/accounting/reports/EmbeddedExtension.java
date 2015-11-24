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

	Map<String,Object> fragments = new HashMap<String, Object>();
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
