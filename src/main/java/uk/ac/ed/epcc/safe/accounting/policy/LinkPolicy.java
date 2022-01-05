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
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.policy;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.MatchSelectVisitor;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory.Use;
import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.IncrementalPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ReferenceFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.TableInput;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.timer.TimeClosable;
/** LinkPolicy is used to cross link two accounting tables when the raw data is spread across 
 * two different sources. For example when using a globus-jobmanager to submit jobs to a batch system.
 * Most of the required data is in the batch accounting log but information about the connection credentials is only available in
 * the job-manager log.
 * <p>
 * One table is defined as the primary table and data is loaded to this table first.
 * This policy is added to the secondary table. It will generate a SkipRecord exception if a corresponding 
 * primary record is not found (This is to protect against the data being loaded out of sequence).
 * This will prevent this policy from working with a {@link IncrementalPropertyContainerParser}
 * <p>
 * Configuration Properties:
 * <ul>
 * <li> <b>LinkPolicy.target.<i>table-name</i></b> defines the remote table we are linking to.</li>
 * <li> <b>LinkPolicy.link.<i>table-name</i>.<i>local-prop</i></b> defines a property name in the remote table
 *          that has to match <i>local-prop</i> in the local table. The special value <b>inside</b> means that the property is a data property 
 *          that should lie between start and end of the parent record as defined using the {@link StandardProperties}.</li>
 * <li> <b>LinkPolicy.grace.<i>table-name</i></b> defines a matching threshold (in seconds) for time properties.
 *         Properties are taken as matching if they are within the specified threshold. Defaults to 4000 seconds.</li>
 * <li> <b>LinkPolicy.require_link.<i>table-name</i></b> can be set to false if we want to allow records without valid matching properties to be parsed.</li>
 * <li> <b>LinkPolicy.copy_properties.<i>table-name</i></b> can be set to false to disable property copying to the primary table.</li>
 * 
 * 
 * </ul>
 * 
 * If a match is found the ReferenceProperty to the primary table is set in the parse method.
 * Then in the PostCreate method the remote object is updated to set the back-reference (if there is one)
 * and any common properties (based on simple names) that are not already set. 
 * Only properties from previous (parser/policies) that are writable in the primary table will be copied.
 * If you want to define expressions that make primary properties available in the secondary table do this in policies installed
 * later than the LinkPolicy   
 * <p>
 * Note that as the mapping between primary and secondary tables is expected to be one-to-one, 
 * at the very least this makes joins inefficient but there may also be serious performance and
 * memory problems if the secondary table is used in code written assuming small classifier tables.
 * Properties that may be used to group queries should therefore be duplicated in the primary table
 * where they can be set by the PostCreate method.
 * <p> 
 * If the mapping is not one-to-one for example a log of executables run inside a batch job then it does not make sense to copy properties into the main table.
 * <p>
 * If the mapping is one-to-one 
 * you should also set the <b>LinkPolicy.unique.<i>table-name</i></b> property to true before the table is created so the table
 * index (containing references to the primary) is created as a unique key,
 * @author spb
 * @param <R> type of primary record
 *
 */


public class LinkPolicy<R extends Use> extends BaseUsageRecordPolicy implements SummaryProvider,TableTransitionContributor,ConfigParamProvider {

	public LinkPolicy(AppContext conn) {
		super(conn);
	}

	private static final String LINK_POLICY_UNIQUE = "LinkPolicy.unique.";
	private static final String LINK_POLICY_LINK = "LinkPolicy.link.";
	private static final String LINK_POLICY_REQUIRE_LINK = "LinkPolicy.require_link.";
	private static final String LINK_POLICY_COPY_PROPERTIES = "LinkPolicy.copy_properties.";
	private static final String LINK_POLICY_GRACE = "LinkPolicy.grace.";
	private static final String LINK_POLICY_TARGET = "LinkPolicy.target.";
	
	private ReferenceTag remote_tag=null;
	private ReferenceTag back_ref=null;
	private UsageRecordFactory<R> remote_fac=null;
	private ExpressionTargetFactory<R> remote_etf=null;
	private Map<PropertyTag,PropertyTag> match_map=null;
	private Map<PropertyTag,PropertyTag> copy_properties=null;
	private Set<PropertyTag> inside_date_properties=null;
	private long grace_millis;
	private boolean require_link=true;
	private boolean copy_props=true;
	private Logger log;
	private boolean use_cache=false;
	private String my_table;
	
	private R last_peer=null;
	@SuppressWarnings("unchecked")
	@Override
	public PropertyFinder initFinder( PropertyFinder prev,
			String table) {
		AppContext c = getContext();
		my_table=table;
		grace_millis = 1000L * c.getLongParameter(LINK_POLICY_GRACE+table, 4000L);
		require_link = c.getBooleanParameter(LINK_POLICY_REQUIRE_LINK+table, true);
		copy_props = c.getBooleanParameter(LINK_POLICY_COPY_PROPERTIES+table, true);
		log = c.getService(LoggerService.class).getLogger(getClass());
		//System.out.println("grace is "+grace_millis);
		String target_name = c.getInitParameter(LINK_POLICY_TARGET+table);
		PropertyTag<? extends IndexedReference>tag = prev.find(IndexedReference.class,target_name );
		if( tag instanceof ReferenceTag ){
			ReferenceTag ref_tag = (ReferenceTag) tag;
			if( UsageRecordFactory.class.isAssignableFrom(ref_tag.getFactoryClass())){
			   remote_tag=ref_tag;
			   remote_fac=(UsageRecordFactory) remote_tag.getFactory(c);
			   remote_etf = ExpressionCast.getExpressionTargetFactory(remote_fac);
			   match_map = new HashMap<>();
			   inside_date_properties = new HashSet<>();
			   copy_properties = new HashMap<>();
			   String prefix =LINK_POLICY_LINK+table+".";
			   PropertyFinder remote_finder=remote_fac.getFinder();
			   for(PropertyTag local_tag : prev.getProperties()){
				   String local_name = local_tag.getName();
				   // we may have multiple local tags with the same simple name so
				   // alwas store the first one in the search path
				   
				   PropertyTag store_tag = prev.find(local_name);
				   if( store_tag != null){
					   String remote_name = c.getInitParameter(prefix+local_name, null);
					   if( remote_name != null ){
						   if( remote_name.equals("inside") ){
							   // This should be a date property that lives inside the standard time bounds
							   // of the parent record
							   inside_date_properties.add(store_tag);
						   }else{
							   // match property
							   PropertyTag remote_tag = remote_finder.find(remote_name);
							   if( remote_tag != null ){
								   match_map.put(store_tag,remote_tag);
							   }
						   }
					   }else if( copy_props){
						   
						   // may-be copy property 
						   // if remote table has a writable property with same simple name
						   // assume they should be copied.
						   PropertyTag remote_tag = remote_finder.find(local_name);

						   if( remote_tag != null && remote_tag.allowExpression(store_tag) &&remote_fac.hasProperty(remote_tag) && remote_etf.getAccessorMap().writable(remote_tag)){
							   copy_properties.put(store_tag, remote_tag);
						   }
					   }
				   }
			   }
			   back_ref = (ReferenceTag) remote_finder.find(IndexedReference.class,table);
			 
			}
		}
		
		if( remote_tag == null ){
			getLogger().error("LinkPolicy target not a RefenceTag to a UsageRecordFactory "+tag);
			return null;
		}
		use_cache = ! c.getBooleanParameter(LINK_POLICY_UNIQUE+table, false);
		// this policy defines no new properties.
		return null;
	}
	public R getPrimary(DerivedPropertyMap rec) throws AccountingParseException {
		try(TimeClosable t = new TimeClosable(getContext(), "LinkPolicy.getPrimary")){
			AndRecordSelector sel = new AndRecordSelector();
			for(Map.Entry<PropertyTag, PropertyTag> entry : match_map.entrySet()) {
				PropertyTag local = entry.getKey();
				PropertyTag remote = entry.getValue();
				Object o = rec.getProperty(local);
				if( o == null ){
					if( require_link){
						throw new AccountingParseException("Link Property "+local.getFullName()+" is null");
					}else{
						return null;
					}
				}
				if( remote.getTarget() == Date.class && grace_millis != 0L){
					Date point = (Date) rec.getProperty(local);
					if( point != null ){
						// might be a partial record
						sel.add(new SelectClause(remote,MatchCondition.GT, new Date(point.getTime()-grace_millis)));
						sel.add(new SelectClause(remote,MatchCondition.LT, new Date(point.getTime()+grace_millis)));
					}else{
						// assume partial record so skip this policy
						return null;
					}
				}else{

					sel.add(new SelectClause(remote,rec.getProperty(local)));
				}
			}
			for(PropertyTag date : inside_date_properties){
				Date point = (Date) rec.getProperty(date);
				if( point != null){
					// might be a partial record
					sel.add(new SelectClause(StandardProperties.ENDED_PROP,MatchCondition.GE, new Date(point.getTime()-grace_millis)));
					sel.add(new SelectClause(StandardProperties.STARTED_PROP,MatchCondition.LE, new Date(point.getTime()+grace_millis)));
				}else{
					// assume partial record so just skip policy
					return null;
				}
			}
			if( last_peer != null && use_cache) {
				// use_cache is true so we are expecting multiple records
				// to map to the same primary. Check the last primary we 
				// used. Assumption is that matching the selector programmatically
				// is going to be faster than searching a large database table.
				MatchSelectVisitor<ExpressionTargetContainer> vis = new MatchSelectVisitor<>(last_peer.getProxy());
				try {
					if( sel.visit(vis)) {
						return last_peer;
					}
				} catch (Exception e) {
					log.error("Error matching stored peer against selector",e);
				}
			}

			return remote_fac.find(remote_fac.getFilter(sel),true);
		}catch(Exception e){
			log.error("Error finding peer in LinkPolicy",e);
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.BasePolicy#parse(uk.ac.ed.epcc.safe.accounting.PropertyMap)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void parse(DerivedPropertyMap rec) throws AccountingParseException {
		if( remote_tag != null ){
			R peer = getPrimary(rec);
			if( peer == null ){
				throw new SkipRecord("No link record found");
			}
			last_peer=peer;
			//System.out.println("Found peer");
			try {
				remote_tag.set(rec,peer);
			} catch (InvalidPropertyException e) {
				throw new AccountingParseException("Cannot set peer reference");
			}
		}
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.BasePolicy#postCreate(uk.ac.ed.epcc.safe.accounting.PropertyContainer)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void postCreate(PropertyContainer props, ExpressionTargetContainer r) throws Exception {
		IndexedReference<R> peer_ref = (IndexedReference<R>) props.getProperty(remote_tag, null);
        
        if( back_ref == null && copy_properties.isEmpty()) {
        	// nothing to do
        	return;
        }
        //System.out.println("In post create ");
		if( peer_ref != null && ! peer_ref.isNull()){
			R peer = null;
			if( last_peer != null && peer_ref.getID() == last_peer.getID()) {
				// should be remembered from the parse stage
				peer = last_peer;
			}else {
				peer = remote_fac.find(peer_ref.getID());
			}
			ExpressionTargetContainer proxy = remote_fac.getExpressionTarget(peer);
			if( back_ref != null && proxy.writable(back_ref)){
				log.debug("set back reference");
				
				// if the table has a reference back to us set it.
				IndexedReference<R> self = (IndexedReference<R>) r.getProperty(back_ref);
				proxy.setProperty(back_ref, self);
				
			}else{
				log.debug("No back reference");
			}
			// set all of the copy properties without values
			for(PropertyTag t: copy_properties.keySet()){
				PropertyTag remote_tag = copy_properties.get(t);
				Object old_peer_value = proxy.getProperty(remote_tag,null);
				
				if( old_peer_value == null || ( old_peer_value instanceof IndexedReference && ((IndexedReference)old_peer_value).isNull())){
					Object value = props.getProperty(t);
					if( value != null ){
					  log.debug("setting "+remote_tag.getFullName());
					  proxy.setOptionalProperty(remote_tag, value);
					}
				}
			}
			proxy.commit();
		}
	}

	public void getTableTransitionSummary(ContentBuilder hb,
			SessionService operator) {
		hb.addText("This policy links entries from this table to a parent record in a different table, reference properties between the"+
				" two tables are set if they exist."+
				" Records are only inserted in this table if a matching parent record is found."+
				" Unset properties in the parent table that correspond to local properties will be populated.");
		if( remote_tag == null ){
			hb.addHeading(5,"No parent table set");
		}else{
			hb.addHeading(5,"parent table: "+remote_tag.getTable());
		}
		hb.addHeading(6,"Match properties");
		
		hb.addTable(operator.getContext(),makeMappingTable(match_map));
		hb.addHeading(6,"Copy properties");
		hb.addTable(operator.getContext(),makeMappingTable(copy_properties));
	}
	private Table makeMappingTable(Map<PropertyTag,PropertyTag> map){
		Table<String,PropertyTag> t = new Table<>();
		if( map != null ){
			for(PropertyTag key : map.keySet()){
				t.put("Local property", key, key.getFullName());
				t.put("Remote property", key, map.get(key).getFullName());
			}
		}
		return t;
	}
	public final class SetRemoteAction extends FormAction{

		private final DataObjectFactory target;
		public SetRemoteAction(DataObjectFactory target) {
			this.target=target;
		}

		@Override
		public FormResult action(Form f)
				throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {

			ConfigService serv = getContext().getService(ConfigService.class);
			serv.setProperty(LINK_POLICY_TARGET+target.getConfigTag(),(String) f.get("table"));
			return new ViewTableResult(target);
			
		}
		
	}
	public class SetRemoteTransition extends AbstractFormTransition<DataObjectFactory>{

		public void buildForm(Form f, DataObjectFactory target,
				AppContext conn) throws TransitionException {
			TableInput<UsageRecordFactory> input = new TableInput<>(conn,UsageRecordFactory.class);
			f.addInput("table", "parent table", input);
			if( remote_tag != null ){
				f.put("table", remote_tag.getTable());
			}
			f.addAction("Set parent", new SetRemoteAction(target));
		}
	}
	public Map<TableTransitionKey, Transition> getTableTransitions() {
		Map<TableTransitionKey,Transition> result = new HashMap<>();
		result.put(new AdminOperationKey("Set parent", "Set the parent table for LinkPolicy"), new SetRemoteTransition());
		return result;
	}
	

	@Override
	public TableSpecification modifyDefaultTableSpecification(TableSpecification spec,
			PropExpressionMap map, String table_name) {
	
		TableSpecification result = super.modifyDefaultTableSpecification(spec, map, table_name);
		AppContext c = getContext();
		String remote_table = c.getInitParameter(LINK_POLICY_TARGET+table_name);
		if( remote_table != null){
			String name = remote_table+"ID";
			spec.setField(name, new ReferenceFieldType(remote_table));
		
			try {
				// Add an index to optimise back-joins from the parent table to this one.
				spec.new Index(name+"_idx", c.getBooleanParameter(LINK_POLICY_UNIQUE+table_name, false), name);
			} catch (InvalidArgument e) {
				getLogger().error("Error making index",e);
			}
		}
		return result;
	}

	@Override
	public void addConfigParameters(Set<String> params) {
		if( my_table != null ) {
		params.add(LINK_POLICY_GRACE+my_table);
		params.add(LINK_POLICY_LINK+my_table);
		params.add(LINK_POLICY_REQUIRE_LINK+my_table);
		params.add(LINK_POLICY_TARGET+my_table);
		params.add(LINK_POLICY_UNIQUE+my_table);
		}
		
	}
	@Override
	public String endParse() {
		last_peer = null;
		return super.endParse();
	}
	@Override
	public void startParse(PropertyContainer staticProps) throws Exception {
		last_peer=null;
	}
}