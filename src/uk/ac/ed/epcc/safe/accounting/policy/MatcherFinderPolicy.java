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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PropertyTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.SetInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.MessageResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ReferenceFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionTarget;
import uk.ac.ed.epcc.webapp.jdbc.table.TransitionSource;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.Matcher;
import uk.ac.ed.epcc.webapp.model.MatcherFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** Add Owner tables to raw usage data under the control of config parameters.
 * This is similar to the {@link ClassificationPolicy} except that it targets a {@link MatcherFinder}
 * For each known String PropertyTag if the config property
 * <b>matcher.<em>table-name</em>.</b><em>property-name</em> is defined then this is taken as a remote
 * table name and the corresponding Reference property is set.
 * 
 * 
 * The target Factory needs to implement {@link OwnerFinder}.
 * @author spb
 *
 */


public class MatcherFinderPolicy extends BasePolicy implements Contexed,TransitionSource<TableTransitionTarget> , SummaryProvider{
	private static final String OWNER= "matcher.";
	private MultiFinder result_finder = new MultiFinder();
	private Map<PropertyTag<String>,ReferenceTag> tagmap = new HashMap<PropertyTag<String>,ReferenceTag>();
  
	private final AppContext c;
	
	private Map<PropertyTag<String>,MatcherFinder> finders; 
	
	// Info for edit transitions
	private String prefix;
	private Set<PropertyTag<String>> available_names = new HashSet<PropertyTag<String>>();
	private Set<ReferenceTag> available_refs = new HashSet<ReferenceTag>();
	private String table;
	private final Logger log;
	public MatcherFinderPolicy(AppContext c){
		this.c=c;
        log = c.getService(LoggerService.class).getLogger(getClass());
		
	}
	@SuppressWarnings("unchecked")
	@Override
	public void parse(PropertyMap rec) throws AccountingParseException {

		for(PropertyTag<String> tag : finders.keySet()){
			log.debug("parse clientname for "+tag+" from "+rec.getClass().getCanonicalName());
			String value;
			value=rec.getProperty(tag);
			log.debug("value="+value);
			if( value != null){
					ReferenceTag ctag = tagmap.get(tag);
					DataObject c = finders.get(tag).findOwner(value);
					if( c == null ){
						throw new AccountingParseException("Cannot find/make entry for "+tag+" "+value);
					}
					assert(c != null);
					try {
						ctag.set(rec,c);
					} catch (InvalidPropertyException e) {
						throw new AccountingParseException("Bad classifier",e);
					}
			}
		}

	}
	@Override
	public String endParse() {
		StringBuilder errors = new StringBuilder();
		
		finders.clear();
		finders=null;
		return errors.toString();
	}
	@Override
	public void startParse(PropertyContainer defaults) throws DataException,
			InvalidPropertyException {
		log.debug("MatcherFinderPolicy: start parse");
		finders= new HashMap<PropertyTag<String>,MatcherFinder>();
		for(PropertyTag<String> tag : tagmap.keySet()){
			ReferenceTag ctag = tagmap.get(tag);
			MatcherFinder fac = c.makeObject(MatcherFinder.class,ctag.getTable());
			
			finders.put(tag, fac);
		}
	}
	public AppContext getContext() {
			return c;
	}
	@SuppressWarnings("unchecked")
	public PropertyFinder initFinder(AppContext c, PropertyFinder finder, String table) {
		// Look for any new classifications
		// have to do this at least once for each instance in case we are a new accounting table
		// which will see additional properties
		this.table=table;
		
			prefix = OWNER+table+".";
		
			Hashtable<String,String> params = c.getInitParameters(prefix);
			
			log.debug("intiFinder number of owners starting with ["+prefix+"]="+params.size());
			for( String name : params.keySet() ){
				String prop_name=name.substring(prefix.length());
				log.debug("consider property "+prop_name);
				PropertyTag<String> tag = (PropertyTag<String>) finder.find(String.class, prop_name);
				
				if( tag != null  && ! tagmap.containsKey(tag)){
					
					String prop_tag = params.get(name);
				
				
					if( prop_tag != null && prop_tag.trim().length() != 0){
						log.debug("found new tag "+prop_tag);
						PropertyTag<? extends IndexedReference> target_tag = finder.find(IndexedReference.class, prop_tag);
						if( target_tag == null ){
							log.error("Expected IndexedReferenceTag "+prop_tag+" not found for "+name);
						}else{
							if( target_tag instanceof ReferenceTag){
								ReferenceTag ett = (ReferenceTag) target_tag;
								if( MatcherFinder.class.isAssignableFrom(ett.getFactoryClass())){
									tagmap.put(tag, ett);
								}else{
									log.error("The Factory for ReferenceTag "+target_tag.getFullName()+" is not an OwnerFinder");
								}
							}else{
								log.error("PropertyTag "+target_tag.getFullName()+" not a ReferenceTag");
							}
						}
					}else{
						//log.debug("no new tag");
					}
				}
			}
			for(PropertyTag tag : finder.getProperties()){
				if( tag.getTarget() == String.class && ! tagmap.containsKey(tag)){
					available_names.add(tag);
				}else if( tag instanceof ReferenceTag && ! tagmap.containsKey(tag)){
					available_refs.add((ReferenceTag) tag);
				}
			}

		return result_finder;
	}
	
	public final class AddOwnerAction extends FormAction {
		private TableTransitionTarget target;
		public AddOwnerAction(TableTransitionTarget target){
			this.target=target;
		}
		@SuppressWarnings("unchecked")
		@Override
		public FormResult action(Form f)
				throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
			SetInput<PropertyTag<String>> name_input = (SetInput<PropertyTag<String>>) f.getInput("Name");
			SetInput<ReferenceTag> ref_input = (SetInput<ReferenceTag>) f.getInput("Ref");
			AppContext c = getContext();
			ConfigService serv = c.getService(ConfigService.class);
			serv.setProperty(prefix+name_input.getValue(), ref_input.getItem().getFullName());
			return new ViewTableResult(target);
		}
	}
	public class AddOwnerTransition extends AbstractFormTransition<TableTransitionTarget>{

		

		public void buildForm(Form f, TableTransitionTarget target,
				AppContext c) throws TransitionException {
			SetInput<PropertyTag<String>> name_input = new SetInput<PropertyTag<String>>();
			for(PropertyTag<String> t : available_names){
				name_input.addChoice(t);
			}
			f.addInput("Name", "String to classify", name_input);
			SetInput<ReferenceTag> ref_input = new SetInput<ReferenceTag>();
			for(ReferenceTag t : available_refs){
				ref_input.addChoice(t);
			}
			f.addInput("Ref", "Reference to Set", ref_input);
			f.addAction("Add", new AddOwnerAction(target));
			
		}

	}
	public final class DeleteOwnerAction extends FormAction {
		private TableTransitionTarget target;
		public DeleteOwnerAction(TableTransitionTarget target){
			this.target=target;
		}
@SuppressWarnings("unchecked")
@Override
public FormResult action(Form f)
		throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
	SetInput<PropertyTag<String>> name_input = (SetInput<PropertyTag<String>>) f.getInput("Name");
	AppContext c = getContext();
	ConfigService serv = c.getService(ConfigService.class);
	serv.setProperty(prefix+name_input.getValue(), "");
	return new ViewTableResult(target);
}
}
	public class DeleteOwnerTransition extends AbstractFormTransition<TableTransitionTarget>{

	

		public void buildForm(Form f, TableTransitionTarget target,
				AppContext c) throws TransitionException {
			SetInput<PropertyTag<String>> name_input = new SetInput<PropertyTag<String>>();
			for(PropertyTag<String> t : tagmap.keySet()){
				name_input.addChoice(t);
			}
			f.addInput("Name", "Matcher to remove", name_input);
			f.addAction("Remove", new DeleteOwnerAction(target));
			
		}
	}
	public final class RegenerateOwnerAction  extends FormAction{
		private TableTransitionTarget target;
		public RegenerateOwnerAction(TableTransitionTarget target){
			this.target=target;
		}

		@SuppressWarnings("unchecked")
		@Override
		public FormResult action(Form f)
				throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
			PropertyTag<String> name=((SetInput<PropertyTag<String>>) f.getInput("Name")).getItem();
			try {
				regenerate(name);
			} catch (Exception e) {
				log.error("Error in Owner regenerate",e);
				return new MessageResult("internal_error");
			}
			return new ViewTableResult(target);
		}
		
		
	}
	public class RegenerateOwnerTransition extends AbstractFormTransition<TableTransitionTarget>{

		public void buildForm(Form f, TableTransitionTarget target,
				AppContext conn) throws TransitionException {
			SetInput<PropertyTag<String>> name_input = new SetInput<PropertyTag<String>>();
			if( target instanceof PropertyTargetGenerator){
				
				for(PropertyTag<String> t : tagmap.keySet()){
					name_input.addChoice(t);
				}
			}
			f.addInput("Name", "Matcher to regenerate", name_input);
			f.addAction("Regenerate", new RegenerateOwnerAction(target));
			
		}
	}
	public Map<TableTransitionKey<TableTransitionTarget>, Transition<TableTransitionTarget>> getTransitions() {
		Map<TableTransitionKey<TableTransitionTarget>,Transition<TableTransitionTarget>> result = new HashMap<TableTransitionKey<TableTransitionTarget>, Transition<TableTransitionTarget>>();
		result.put(new AdminOperationKey<TableTransitionTarget>(TableTransitionTarget.class, "AddMatcher"), new AddOwnerTransition());
		result.put(new AdminOperationKey<TableTransitionTarget>(TableTransitionTarget.class, "RemoveMatcher"), new DeleteOwnerTransition());
		result.put(new AdminOperationKey<TableTransitionTarget>(TableTransitionTarget.class,"RegenerateMatcher","Regenenerate matcher references"),new RegenerateOwnerTransition());
		return result;
	}
	public void getTableTransitionSummary(ContentBuilder hb,
			SessionService operator) {
		hb.addText("This policy generates references to a entries in an Owner table based"
				+" on a string property.");
		try{
			Table<String,String> t = new Table<String,String>();
			for(PropertyTag<String> name : tagmap.keySet()){
				t.put("Matcher",name.getFullName(),tagmap.get(name));
			}
			t.setKeyName("Source");
			t.sortRows();
			if( t.hasData()){
				hb.addTable(getContext(), t);
			}
		}catch(Throwable e){
			log.error("Error making ClassificationPolicy summary table",e);
		}
	}
	@SuppressWarnings("unchecked")
	private void regenerate(PropertyTag<String> name) throws Exception {
		
		PropertyTargetGenerator<?> fac = getContext().makeObjectWithDefault(PropertyTargetGenerator.class, null, table);
		ReferenceTag ctag = tagmap.get(name);
		MatcherFinder<?> matcherFinder = c.makeObject(MatcherFinder.class,ctag.getTable());
		
		if( matcherFinder == null ){
			return;
		}
		Set<? extends Matcher> set = matcherFinder.getOwners();
		ReferenceTag ref = tagmap.get(name);
		if( fac != null ){
			Iterator<?> it = fac.getIterator(new AndRecordSelector());
			while(it.hasNext()){
				PropertyTarget rec = (PropertyTarget) it.next();
				String clientName = rec.getProperty(name, null);
				if( clientName != null ){
					for(Matcher m : set){
						if(m.matches(clientName)){
							ref.set((PropertyContainer) rec, m);
							((DataObject)rec).commit();
						}
					}
				}
			}

		}
		
	}
	@Override
	public TableSpecification modifyDefaultTableSpecification(AppContext conn,TableSpecification t,PropExpressionMap map,String tag_name) {
	    
		if( t != null ){ 
			
			String prefix = OWNER+tag_name+".";
			
			Hashtable<String,String> params = c.getInitParameters(prefix);
			
			for(String table : params.values()){
				
				String name = table+"ID";
				if( t.goodFieldName(name)){
					t.setField(name, new ReferenceFieldType(table));
				}else{
					conn.getService(LoggerService.class).getLogger(getClass()).error("Bad field name "+name+" in ClassificationPolicy");
				}
			}
		}
		return t;
	}
}