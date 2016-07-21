package uk.ac.ed.epcc.safe.accounting.policy;

import java.util.Iterator;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.db.RegexpTarget;
import uk.ac.ed.epcc.safe.accounting.db.RegexpTargetFactory;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** A policy to invoke a nested parse on a String property generated by the parent parse.
 * 
 * @author spb
 *
 */

public class RegexLinkParsePolicy extends BaseUsageRecordPolicy {
	
	AppContext conn;
	String table_name, target_table_name;
	PropertyTag<String> link_prop;
	Map<String, Object> target_map;
	ReferenceTag target_tag;
	
	public RegexLinkParsePolicy() {
	}

	
	@Override
	public PropertyFinder initFinder(AppContext conn, PropertyFinder prev, String table) {
		this.conn = conn;
		this.table_name = table;
		
		String prop_name = conn.getInitParameter("regex_link_parse.prop." + table_name);
		target_table_name = conn.getInitParameter("regex_link_parse.table." + table_name);
		
		if( prop_name != null && target_table_name != null){
			
			ReferencePropertyRegistry registry = ReferencePropertyRegistry.getInstance(conn);
			target_tag = (ReferenceTag) registry.find(target_table_name);
			
			link_prop = (PropertyTag<String>) prev.find(String.class, prop_name);
				
			RegexpTargetFactory rt_fac = new RegexpTargetFactory(conn, target_table_name);
			Integer rt_key = 0;
			Iterator targets;
			try {
				targets = rt_fac.all().iterator();
				while (targets.hasNext()) {
					RegexpTarget rt = (RegexpTarget) targets.next();
					target_map.put(rt_key.toString(), rt.getReference());
					rt_key++;
				}
			} catch (DataFault e) {
				e.printStackTrace();
			}
			
		}
		
		return prev;
	}
	
	@Override
	public void parse(PropertyMap rec) throws AccountingParseException {
		
		String linked_name = rec.getProperty(link_prop);

		Iterator targets = target_map.entrySet().iterator();
		while (targets.hasNext()) {
			RegexpTarget tar = (RegexpTarget) targets.next();
			if (linked_name.matches(tar.getRegexp().toString())) {
				rec.setProperty(target_tag, tar.getReference());
				break;
			}
		}
		
	}
	

}

