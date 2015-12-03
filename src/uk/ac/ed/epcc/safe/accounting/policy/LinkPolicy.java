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
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory.Use;
import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
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
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionTarget;
import uk.ac.ed.epcc.webapp.jdbc.table.TransitionSource;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.TableInput;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.data.transition.TransitionKey;
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
 *          that has to match <i>local-prop</i> in the local table.
 * <li> <b>LinkPolicy.grace.<i>table-name</i></b> defines a matching threshold (in seconds) for time properties.
 *         Properties are taken as matching if they are within the specified threshold. Defaults to 4000 seconds.
 * </ul>
 * 
 * If a match is found the ReferenceProperty to the primary table is set in the parse method.
 * Then in the PostCreate method the remote object is updated to set the back-reference (if there is one)
 * and the any common properties (based on simple names) that are not already set.
 * <p>
 * Note that as the mapping between primary and secondary tables is expected to be one-to-one, 
 * at the very least this makes joins inefficient but there may also be serious performance and
 * memory problems if the secondary table is used in code written assuming small classifier tables.
 * Properties that may be used to group queries should therefore be duplicated in the primary table
 * where they can be set by the PostCreate method.
 * @author spb
 *
 */


public class LinkPolicy extends BaseUsageRecordPolicy implements SummaryProvider,TransitionSource<TableTransitionTarget> {

	private static final String LINK_POLICY_TARGET = "LinkPolicy.target.";
	private AppContext c;
	private ReferenceTag remote_tag=null;
	private ReferenceTag back_ref=null;
	private UsageRecordFactory<?> remote_fac=null;
	private Map<PropertyTag,PropertyTag> match_map=null;
	private Map<PropertyTag,PropertyTag> copy_properties=null;
	private long grace_millis;
	private Logger log;
	@SuppressWarnings("unchecked")
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		c = ctx;
		grace_millis = 1000L * ctx.getLongParameter("LinkPolicy.grace."+table, 4000L);
		log = c.getService(LoggerService.class).getLogger(getClass());
		//System.out.println("grace is "+grace_millis);
		String target_name = c.getInitParameter(LINK_POLICY_TARGET+table);
		PropertyTag<? extends IndexedReference>tag = prev.find(IndexedReference.class,target_name );
		if( tag instanceof ReferenceTag ){
			ReferenceTag ref_tag = (ReferenceTag) tag;
			if( UsageRecordFactory.class.isAssignableFrom(ref_tag.getFactoryClass())){
			   remote_tag=ref_tag;
			   remote_fac=(UsageRecordFactory) remote_tag.getFactory(c);
			   match_map = new HashMap<PropertyTag, PropertyTag>();
			   copy_properties = new HashMap<PropertyTag,PropertyTag>();
			   String prefix ="LinkPolicy.link."+table+".";
			   PropertyFinder remote_finder=remote_fac.getFinder();
			   for(PropertyTag local_tag : prev.getProperties()){
				   String local_name = local_tag.getName();
				   // we may have multiple local tags with the same simple name so
				   // alwas store the first one in the search path
				   
				   PropertyTag store_tag = prev.find(local_name);
				   String remote_name = ctx.getInitParameter(prefix+local_name, null);
				   if( remote_name != null ){
					 
				       // match property
				       PropertyTag remote_tag = remote_finder.find(remote_name);
				      if( remote_tag != null ){
					    match_map.put(store_tag,remote_tag);
				      }
				   }else{
					   // may-be copy property 
					   // if remote table has a property with same simple name
					   // assume they should be copied.
					   PropertyTag remote_tag = remote_finder.find(local_name);
					
					   if( remote_tag != null && remote_fac.hasProperty(remote_tag) ){
						   copy_properties.put(store_tag, remote_tag);
					   }
				   }
			   }
			   back_ref = (ReferenceTag) remote_finder.find(IndexedReference.class,table);
			 
			}
		}
		
		if( remote_tag == null ){
			ctx.error("LinkPolicy target not a RefenceTag to a UsageRecordFactory "+tag);
			return null;
		}
		
		// this policy defines no new policies.
		return null;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.BasePolicy#parse(uk.ac.ed.epcc.safe.accounting.PropertyMap)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void parse(PropertyMap rec) throws AccountingParseException {
		if( remote_tag != null ){
		   AndRecordSelector sel = new AndRecordSelector();
		   for(PropertyTag local : match_map.keySet()){
			   PropertyTag remote = match_map.get(local);
			   Object o = rec.getProperty(local);
			   if( o == null ){
				   throw new AccountingParseException("Link Property "+local.getFullName()+" is null");
			   }
			   if( remote.getTarget() == Date.class && grace_millis != 0L){
				   Date point = (Date) rec.getProperty(local);
				   sel.add(new SelectClause(remote,MatchCondition.GT, new Date(point.getTime()-grace_millis)));
				   sel.add(new SelectClause(remote,MatchCondition.LT, new Date(point.getTime()+grace_millis)));
			   }else{
				
				   sel.add(new SelectClause(remote,rec.getProperty(local)));
			   }
		   }
		   UsageRecordFactory.Use peer = null;
		   try{
			    peer =remote_fac.find(sel);
		   }catch(Exception e){
			     c.error(e,"Error finding peer in LinkPolicy");
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
	public void postCreate(PropertyContainer props, UsageRecord r) throws Exception {
		IndexedReference<UsageRecordFactory.Use> peer_ref = (IndexedReference<Use>) props.getProperty(remote_tag, null);
        Use rec = (Use) r;
        
        //System.out.println("In post create ");
		if( peer_ref != null && peer_ref.getID() > 0){
			Use peer = remote_fac.find(peer_ref.getID());
			if( back_ref != null && peer.writable(back_ref)){
				log.debug("set back reference");
				
				// if the table has a reference back to us set it.
				back_ref.set(peer, rec);
				
			}else{
				log.debug("No back reference");
			}
			// set all of the copy properties without values
			for(PropertyTag t: copy_properties.keySet()){
				PropertyTag remote_tag = copy_properties.get(t);
				Object old_peer_value = peer.getProperty(remote_tag,null);
				
				if( old_peer_value == null || ( old_peer_value instanceof IndexedReference && ((IndexedReference)old_peer_value).isNull())){
					Object value = props.getProperty(t);
					if( value != null ){
					  log.debug("setting "+remote_tag.getFullName());
					  peer.setOptionalProperty(remote_tag, value);
					}
				}
			}
			peer.commit();
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

		private final TableTransitionTarget target;
		public SetRemoteAction(TableTransitionTarget target) {
			this.target=target;
		}

		@Override
		public FormResult action(Form f)
				throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {

			ConfigService serv = c.getService(ConfigService.class);
			//TODO is there a better way of getting the config tag
			serv.setProperty(LINK_POLICY_TARGET+target.getTableTransitionID(),(String) f.get("table"));
			return new ViewTableResult(target);
			
		}
		
	}
	public class SetRemoteTransition extends AbstractFormTransition<TableTransitionTarget>{

		public void buildForm(Form f, TableTransitionTarget target,
				AppContext conn) throws TransitionException {
			TableInput<UsageRecordFactory> input = new TableInput<UsageRecordFactory>(conn,UsageRecordFactory.class);
			f.addInput("table", "Master table", input);
			if( remote_tag != null ){
				f.put("table", remote_tag.getTable());
			}
			f.addAction("Set Master", new SetRemoteAction(target));
		}
	}
	public Map<TransitionKey<TableTransitionTarget>, Transition<TableTransitionTarget>> getTransitions() {
		Map<TransitionKey<TableTransitionTarget>,Transition<TableTransitionTarget>> result = new HashMap<TransitionKey<TableTransitionTarget>, Transition<TableTransitionTarget>>();
		result.put(new TransitionKey<TableTransitionTarget>(TableTransitionTarget.class, "Set Master", "Set the master table for LinkPolicy"), new SetRemoteTransition());
		return result;
	}
}