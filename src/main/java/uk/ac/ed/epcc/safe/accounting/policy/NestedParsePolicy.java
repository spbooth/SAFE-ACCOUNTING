package uk.ac.ed.epcc.safe.accounting.policy;

import java.io.ByteArrayInputStream;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.AccountingUpdater;
import uk.ac.ed.epcc.safe.accounting.db.PropertyContainerParseTargetComposite;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory.Use;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordParseTarget;
import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** A policy to invoke a nested parse on a String property generated by the parent parse.
 * 
 * A String value is extracted from the parse 
 * (property name defined in <b>nested_parse.prop.<i>table-name</i></b>)
 * A nested parse is then applied to this string and stored in the table 
 * <b>nested_parse.table.<i>table-name</i></b>
 * To allow the nested table to back-reference to the parent table a reference to the parent record is added
 * to the set of externally defined properties passed to the parse (the target table must include the reference
 * registry and contain a reference field to the parent.
 * 
 * Optionally if the property <b>nested_parse.link.<i>table-name</i></b> is set to the name of an
 * integer property the value of that property is pre-pended to the parse string (separated by a space).
 * 
 * @author spb
 *
 */

public class NestedParsePolicy extends BaseUsageRecordPolicy implements SummaryProvider,ConfigParamProvider {
	
	private static final String NESTED_PARSE_TABLE_PREFIX = "nested_parse.table.";
	private static final String NESTED_PARSE_PROP_PREFIX = "nested_parse.prop.";
	private static final String NESTED_PARSE_LINK_PREFIX = "nested_parse.link.";

	String table_name, nested_table_name;
	String link_prop_name;
	PropertyTag<Integer> link_prop;
	PropertyTag<String> nested_prop;
	ReferenceTag parent_tag;
	UsageRecordFactory parent_fac;
	UsageRecordParseTarget<String> parse_target;
	
	public NestedParsePolicy(AppContext conn) {
		super(conn);
	}

	
	@Override
	public PropertyFinder initFinder(PropertyFinder prev, String table_name) {
		
	
		this.table_name = table_name;
		
		link_prop_name = conn.getInitParameter(NESTED_PARSE_LINK_PREFIX + table_name);
		if( link_prop_name != null ){
			link_prop = (PropertyTag<Integer>) prev.find(Integer.class, link_prop_name);
		}
		
		String prop_name = conn.getInitParameter(NESTED_PARSE_PROP_PREFIX + table_name);
		if( prop_name != null ){
			nested_prop = (PropertyTag<String>) prev.find(String.class, prop_name);
			nested_table_name = conn.getInitParameter(NESTED_PARSE_TABLE_PREFIX + table_name);
			parse_target = conn.makeObject(UsageRecordParseTarget.class, nested_table_name);
			if( parse_target == null ){
	        	DataObjectFactory<?> fac = conn.makeObject(DataObjectFactory.class,nested_table_name);
	        	if( fac != null) {
	        		PropertyContainerParseTargetComposite comp = fac.getComposite(PropertyContainerParseTargetComposite.class);
	        		if( comp != null && comp instanceof UsageRecordParseTarget) {
	        			parse_target = (UsageRecordParseTarget) comp;
	        		}
	        	}
	        	if( parse_target == null) {
	        		getLogger().warn("Table "+nested_table_name+" does not have an accounting table configured");
	        	}
	        }
		}
		
		ReferencePropertyRegistry registry = ReferencePropertyRegistry.getInstance(conn);
		parent_tag = (ReferenceTag) registry.find(table_name);
	
		
		return prev;
		
	}

	
	@Override
	public void postCreate(PropertyContainer props, ExpressionTargetContainer rec) throws Exception {
		
		if( nested_prop != null && parse_target != null){
			
			String update = rec.getProperty(nested_prop);
			if( update != null ){
				
				if (link_prop_name != null) {
				    update = link_prop_name + "=" + rec.getProperty(link_prop).toString() + " " + update;
				}
				
				PropertyMap meta_data = new PropertyMap();      
			    meta_data.setAll(props);
			    if (parent_tag != null) {
			    	meta_data.setProperty(parent_tag, rec.getProperty(parent_tag));
			    }
				
				AccountingUpdater<Use, String> updater = new AccountingUpdater<>(conn, meta_data, parse_target, true, false, false);
				updater.receiveAccountingData(new ByteArrayInputStream( update.getBytes()));
				
			}
			
		}
		
	}


	@Override
	public void getTableTransitionSummary(ContentBuilder hb, SessionService operator) {
		hb.addText("This policy performs a nested parse on a value generated by the parser");
		hb.addText("Optionally it adds a link property to the string so the parser can locate its parent record");
		Table t = new Table();
		t.put("Value", "Link property", link_prop_name==null ? "Unset" : link_prop_name);
		
		t.put("Value", "Property to parse", nested_prop == null ? "Unset" : nested_prop.getFullName());
		t.put("Value","Table to populate",nested_table_name == null ? "Unset" : nested_table_name);
		t.setKeyName("Property");
		if( t.hasData()){
			hb.addTable(conn, t);
		}
	}


	@Override
	public void addConfigParameters(Set<String> params) {
		params.add(NESTED_PARSE_PROP_PREFIX+table_name);
		params.add(NESTED_PARSE_TABLE_PREFIX+table_name);
		params.add(NESTED_PARSE_LINK_PREFIX+table_name);
		
	}


}
