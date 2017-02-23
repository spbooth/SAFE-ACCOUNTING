package uk.ac.ed.epcc.safe.accounting.policy;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.RegexpTarget;
import uk.ac.ed.epcc.safe.accounting.db.RegexpTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.ExtendedXMLBuilder;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractDirectTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.DatabaseService;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ReferenceFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionTarget;
import uk.ac.ed.epcc.webapp.jdbc.table.TransitionSource;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.data.transition.TransitionKey;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** A policy to links to entries in a {@link RegexpTargetFactory}.
 * 
 * Config parameters:
 * <ul>
 * <li><b>regex_link_parse.link.<em>table-name</em></b> Name of the String property to apply the regexp to.</li>
 * <li><b>regex_link_parse.table.<em>table-name</em></b> Name of the {@link RegexpTargetFactory} to link to. This should also be the name of the linking property that is set by the policy</li>
 
 * </ul>
 * 
 * @author spb
 *
 */

public class RegexLinkParsePolicy extends BaseUsageRecordPolicy  implements SummaryProvider,TransitionSource<TableTransitionTarget>{
	private AppContext conn;
	private String table;
	private String target_table;
	private PropertyTag<String> link_prop;
	private ReferenceTag target_prop;
	private Set<RegexpTarget> targets; 
	private int links,records;
	
	public RegexLinkParsePolicy() {
	}
	
	@Override
	public PropertyFinder initFinder(AppContext conn, PropertyFinder prev, String table) {
		this.conn=conn;
		this.table=table;
		Logger log = getLogger(conn);
		ReferencePropertyRegistry finder = ReferencePropertyRegistry.getInstance(conn);
		try {
				
			String link_name = conn.getInitParameter("regex_link_parse.link." + table);
			if (null == link_name) {
				throw new AccountingParseException("Error, regex_link_parse.link." + table + " not set.");
			}
			link_prop = (PropertyTag<String>) prev.find(String.class, link_name);
			if (null == link_prop) {
				log.error("Error, cannot find " + link_name + " property.");
				return prev;
			}
			
			
			
			
			target_table = conn.getInitParameter("regex_link_parse.table." + table);
			if (null == target_table) {
				log.error("Error, regex_link_parse.table." + table + " not set.");
				return prev;
			}	
			PropertyTag prop = finder.find(IndexedReference.class, target_table);
			if( prop != null && prop instanceof ReferenceTag){
				target_prop = (ReferenceTag) prop;
			}else{
				getLogger(conn).error("No target_prop found  for "+target_table);
			}
			
		} catch (Exception e) {
			getLogger(conn).error("Error initialising RegexLinkParsePolicy",e);
		}
		
		return finder;
	}
	
	@Override
	public void parse(PropertyMap rec) throws AccountingParseException {
		records++;
		
		if( link_prop == null || target_prop == null){
			return;
		}

		process(rec);
	}

	/**
	 * @param rec
	 * @throws AccountingParseException
	 */
	public void process(PropertyContainer rec) throws AccountingParseException {
		String link_data;
		try {
			link_data = rec.getProperty(link_prop);
		} catch (InvalidExpressionException e1) {
			throw new AccountingParseException(e1);
		}
		if( link_data != null && ! link_data.isEmpty()){
			for( RegexpTarget tar : targets){

				if( tar.getRegexp().matcher(link_data).matches()) {
					try {
						target_prop.set(rec, tar);
					} catch (InvalidPropertyException e) {
						throw new AccountingParseException(e);
					}
					links++;
					break;
				}
			}
		}
	}

	@Override
	public void startParse(PropertyContainer staticProps) throws Exception {
		targets = new LinkedHashSet<RegexpTarget>();
		RegexpTargetFactory rt_fac = conn.makeObject(RegexpTargetFactory.class,target_table);
		rt_fac.all().toCollection(targets);
		links=0;
		records=0;
	}

	@Override
	public String endParse() {
		targets=null;
		return ""+links+" records out of "+records+" linked";
	}

	@Override
	public TableSpecification modifyDefaultTableSpecification(AppContext c, TableSpecification spec,
			PropExpressionMap map, String table_name) {
		TableSpecification ss = super.modifyDefaultTableSpecification(c, spec, map, table_name);
		String target_table = c.getInitParameter("regex_link_parse.table." + table_name);
		if( target_table != null ){
			ss.setField(target_table+"ID", new ReferenceFieldType(target_table));
		}
		return ss;
	}

	@Override
	public void getTableTransitionSummary(ContentBuilder hb, SessionService operator) {
		hb.addText("This policy add a link to entries in a RegexpTargetFactory. which record is selected is up to that class");
		if( link_prop == null ){
			ExtendedXMLBuilder text = hb.getText();
			text.clean("linking property not set. Set the configuration parameter ");
			text.open("b");
			text.clean("regex_link_parse.link.");
			text.open("em");
			text.clean("table-name");
			text.close();
			text.close();
			text.clean(" to the name of a String property");
			text.appendParent();
		}else{
			hb.addText("Link property is "+link_prop.getFullName());
		}
		if( target_prop == null ){
			ExtendedXMLBuilder text = hb.getText();
			text.clean("target property not set. Set the configuration parameter ");
			text.open("b");
			text.clean("regex_link_parse.table.");
			text.open("em");
			text.clean("table-name");
			text.close();
			text.close();
			text.clean(" to the name of the table you want to point to");
			text.appendParent();
		}else{
			hb.addText("Target property is "+target_prop.getFullName());
		}
	}
	private void relink() throws Exception{
		
		DatabaseService db = conn.getService(DatabaseService.class);
		
			DataObjectFactory<?> fac = conn.makeObjectWithDefault(DataObjectFactory.class, null, table);

			startParse(null);
			if( link_prop == null || target_prop == null){
				return;
			}
			for(DataObject rec : fac.all()){
				process((PropertyContainer) rec);
				rec.commit();
				db.commitTransaction();
			}
		
	}

	public class RelinkTransition extends AbstractDirectTransition<TableTransitionTarget>{

	
		@Override
		public FormResult doTransition(TableTransitionTarget target, AppContext c) throws TransitionException {
			try {
				relink();
			} catch (Exception e) {
				getLogger(conn).error("Error in relink",e);
				throw new TransitionException("internal_error");
			}
			return new ViewTableResult(target);
		}
		
	}
	@Override
	public Map<TableTransitionKey<TableTransitionTarget>, Transition<TableTransitionTarget>> getTransitions() {
		Map<TableTransitionKey<TableTransitionTarget>,Transition<TableTransitionTarget>> result = new HashMap<TableTransitionKey<TableTransitionTarget>, Transition<TableTransitionTarget>>();
		result.put(new AdminOperationKey(TableTransitionTarget.class,"Relink","Re-apply regexp links to all records"),new RelinkTransition());
		return result;
	}
	

}

