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
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.TableInput;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.session.SessionService;
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
 * <li> <b>LinkPolicy.target.<i>table-name</i></b> defines the remote table we are linking to.
 * <li> <b>LinkPolicy.link.<i>table-name</i>.<i>local-prop</i></b> defines a property name in the remote table
 *          that has to match <i>local-prop</i> in the local table. The special value <b>inside</b> means that the property is a data property 
 *          that should lie between start and end of the parent record as defined using the {@link StandardProperties}.
 * <li> <b>LinkPolicy.grace.<i>table-name</i></b> defines a matching threshold (in seconds) for time properties.
 *         Properties are taken as matching if they are within the specified threshold. Defaults to 4000 seconds.
 * <li> <b>LinkPolicy.require_link.<i>table-name</i></b> can be set to false if we don't want to allow records without a valid link to be parsed.
 * </ul>
 * 
 * If a match is found the ReferenceProperty to the primary table is set in the parse method.
 * Then in the PostCreate method the remote object is updated to set the back-reference (if there is one)
 * and the any common properties (based on simple names) that are not already set. 
 * Only properties from previous (parser/policies) that are writable in the primary table will be copied.
 * If you want to define expressions that copy primary properties into the secondary table do this in policies installed
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
 * you should also set the <b>LinkPolicy.unique.<i>table-name</i></b> property to false before the table is created so the table
 * index is created as a unique key,
 * @author spb
 *
 */


public class LinkPolicy<R extends Use> extends BaseUsageRecordPolicy implements SummaryProvider,TableTransitionContributor {

	private static final String LINK_POLICY_TARGET = "LinkPolicy.target.";
	private AppContext c;
	private ReferenceTag remote_tag=null;
	private ReferenceTag back_ref=null;
	private UsageRecordFactory<R> remote_fac=null;
	private ExpressionTargetFactory<R> remote_etf=null;
	private Map<PropertyTag,PropertyTag> match_map=null;
	private Map<PropertyTag,PropertyTag> copy_properties=null;
	private Set<PropertyTag> inside_date_properties=null;
	private long grace_millis;
	private boolean require_link=true;
	private Logger log;
	@SuppressWarnings("unchecked")
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		c = ctx;
		grace_millis = 1000L * ctx.getLongParameter("LinkPolicy.grace."+table, 4000L);
		require_link = ctx.getBooleanParameter("LinkPolicy.require_link."+table, true);
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
			   match_map = new HashMap<PropertyTag, PropertyTag>();
			   inside_date_properties = new HashSet<PropertyTag>();
			   copy_properties = new HashMap<PropertyTag,PropertyTag>();
			   String prefix ="LinkPolicy.link."+table+".";
			   PropertyFinder remote_finder=remote_fac.getFinder();
			   for(PropertyTag local_tag : prev.getProperties()){
				   String local_name = local_tag.getName();
				   // we may have multiple local tags with the same simple name so
				   // alwas store the first one in the search path
				   
				   PropertyTag store_tag = prev.find(local_name);
				   if( store_tag != null){
					   String remote_name = ctx.getInitParameter(prefix+local_name, null);
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
					   }else{
						   
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
		
		// this policy defines no new properties.
		return null;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.BasePolicy#parse(uk.ac.ed.epcc.safe.accounting.PropertyMap)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void parse(DerivedPropertyMap rec) throws AccountingParseException {
		if( remote_tag != null ){
		   AndRecordSelector sel = new AndRecordSelector();
		   for(PropertyTag local : match_map.keySet()){
			   PropertyTag remote = match_map.get(local);
			   Object o = rec.getProperty(local);
			   if( o == null ){
				   if( require_link){
					   throw new AccountingParseException("Link Property "+local.getFullName()+" is null");
				   }else{
					   return;
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
					   return;
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
				   return;
			   }
		   }
		   UsageRecordFactory.Use peer = null;
		   try{
			    peer =remote_fac.find(remote_fac.getFilter(sel),true);
		   }catch(Exception e){
			     getLogger().error("Error finding peer in LinkPolicy",e);
		   }
		   if( peer == null ){
			   throw new SkipRecord("No link record found");
		   }
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
        
        
        //System.out.println("In post create ");
		if( peer_ref != null && peer_ref.getID() > 0){
			R peer = remote_fac.find(peer_ref.getID());
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
		hb.addText("This policy links entries from this table to a master record in a different table, reference properties between the"+
				" two tables are set if they exist."+
				" Records are only inserted in this table if a matching master record is found."+
				" Unset properties in the master table that correspond to local properties will be populated.");
		if( remote_tag == null ){
			hb.addHeading(5,"No master table set");
		}else{
			hb.addHeading(5,"Master table: "+remote_tag.getTable());
		}
		hb.addHeading(6,"Match properties");
		
		hb.addTable(operator.getContext(),makeMappingTable(match_map));
		hb.addHeading(6,"Copy properties");
		hb.addTable(operator.getContext(),makeMappingTable(copy_properties));
	}
	private Table makeMappingTable(Map<PropertyTag,PropertyTag> map){
		Table<String,PropertyTag> t = new Table<String,PropertyTag>();
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

			ConfigService serv = c.getService(ConfigService.class);
			serv.setProperty(LINK_POLICY_TARGET+target.getConfigTag(),(String) f.get("table"));
			return new ViewTableResult(target);
			
		}
		
	}
	public class SetRemoteTransition extends AbstractFormTransition<DataObjectFactory>{

		public void buildForm(Form f, DataObjectFactory target,
				AppContext conn) throws TransitionException {
			TableInput<UsageRecordFactory> input = new TableInput<UsageRecordFactory>(conn,UsageRecordFactory.class);
			f.addInput("table", "Master table", input);
			if( remote_tag != null ){
				f.put("table", remote_tag.getTable());
			}
			f.addAction("Set Master", new SetRemoteAction(target));
		}
	}
	public Map<TableTransitionKey, Transition> getTableTransitions() {
		Map<TableTransitionKey,Transition> result = new HashMap<TableTransitionKey, Transition>();
		result.put(new AdminOperationKey("Set Master", "Set the master table for LinkPolicy"), new SetRemoteTransition());
		return result;
	}
	protected final Logger getLogger(){
		return c.getService(LoggerService.class).getLogger(getClass());
	}

	@Override
	public TableSpecification modifyDefaultTableSpecification(AppContext c, TableSpecification spec,
			PropExpressionMap map, String table_name) {
	
		TableSpecification result = super.modifyDefaultTableSpecification(c, spec, map, table_name);
		String remote_table = c.getInitParameter(LINK_POLICY_TARGET+table_name);
		if( remote_table != null){
			String name = remote_table+"ID";
			spec.setField(name, new ReferenceFieldType(remote_table));
		
			try {
				// Add an index to optimise back-joins from the master table to this one.
				spec.new Index(name+"_idx", c.getBooleanParameter("LinkPolicy.unique."+table_name, false), name);
			} catch (InvalidArgument e) {
				c.getService(LoggerService.class).getLogger(getClass()).error("Error making index",e);
			}
		}
		return result;
	}
}