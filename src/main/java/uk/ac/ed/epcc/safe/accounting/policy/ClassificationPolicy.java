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
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.PropertyUpdater;
import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.NamePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.expr.SelectPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.PropertyTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
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
import uk.ac.ed.epcc.webapp.forms.transition.ExtraFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.forms.transition.TransitionVisitor;
import uk.ac.ed.epcc.webapp.jdbc.DatabaseService;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ReferenceFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.model.NameFinder;
import uk.ac.ed.epcc.webapp.model.data.DataCache;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** Add Classification tables to raw usage data under the control of config parameters.
 * 
 * For each known String PropertyTag if the config property
 * <b>classification.<em>table-name</em>.</b><em>property-name</em> is defined then this is taken as a remote
 * table name and the corresponding Reference property is set.
 * 
 * A derived properties is added for the original string property removing the need to also store the original
 * field in the database.
 * note that this will overwrite any derivations previously in force 
 * 
 * Normally this will be the first or a very early policy as the classification policies will usually be
 * generated from the parser but later policies might want to evaluate expressions on the classification policy 
 * as this is a common location to put local policy configuration parameters
 * 
 * The target Factory needs to implement {@link NameFinder}.
 * @author spb
 *
 */


public class ClassificationPolicy extends BasePolicy implements Contexed,TableTransitionContributor , SummaryProvider{
	private static final String CLASSIFICATION = "classification.";
	private MultiFinder result_finder = new MultiFinder();
	private Map<PropertyTag<String>,ReferenceTag> tagmap = new HashMap<>();
    private PropExpressionMap derived = new PropExpressionMap();
    private PropExpressionMap fallback = new PropExpressionMap();
	
	
	private Map<PropertyTag<String>,DataCache<String,DataObject>> caches; 
	
	// Info for edit transitions
	private String prefix;
	private Set<PropertyTag<String>> available_names = new HashSet<>();
	private Set<ReferenceTag> available_refs = new HashSet<>();
	private String table;
	
	public ClassificationPolicy(AppContext c){
		super(c);
	}
	@SuppressWarnings("unchecked")
	@Override
	public void parse(DerivedPropertyMap rec) throws AccountingParseException {
		Logger log = getLogger();
		for(PropertyTag<String> tag : caches.keySet()){
			log.debug("parse classification for "+tag+" from "+rec.getClass().getCanonicalName());
			String value;
			if( rec instanceof DerivedPropertyMap){
				DerivedPropertyMap dpm = (DerivedPropertyMap) rec;
				value = dpm.getNonDerivedProperty(tag);
				if( value == null ){
					PropExpression<? extends String> exp =  fallback.get(tag);
					if( exp != null){
						try {
							value = dpm.evaluate(String.class, exp);
						} catch (Exception e) {

						}
					}
				}
			}else{
				value=rec.getProperty(tag);
			}
			log.debug("value="+value);
			if( value != null){
					ReferenceTag ctag = tagmap.get(tag);
					DataObject c = caches.get(tag).get(value);
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
		for( PropertyTag<String> tag : caches.keySet()){
		    DataCache<String,? extends DataObject> cache = caches.get(tag);
			for(String key : cache.badKeys()){
				errors.append("Bad classifier :"+key+"\n");
			}
			cache.clear();
		}
		caches.clear();
		caches=null;
		return errors.toString();
	}
	@SuppressWarnings("unchecked")
	@Override
	public void startParse(PropertyContainer defaults) throws DataException,
			InvalidPropertyException {
		Logger log = getLogger();
		log.debug("ClassificationPolicy: start parse");
		caches= new HashMap<>();
		for(PropertyTag<String> tag : tagmap.keySet()){
			ReferenceTag ctag = tagmap.get(tag);
			NameFinder fac = getContext().makeObject(NameFinder.class,ctag.getTable());
			DataCache dataCache = fac.getDataCache();
			assert dataCache != null : "Null datacache";
			caches.put(tag, dataCache);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PropertyFinder initFinder(PropertyFinder finder, String table) {
		// Look for any new classifications
		// have to do this at least once for each instance in case we are a new accounting table
		// which will see additional properties
		this.table=table;
		Logger log = getLogger();
		AppContext c = getContext();
			prefix = CLASSIFICATION+table+".";
		
			Hashtable<String,String> params = c.getInitParameters(prefix);
			
			log.debug("intiFinder number of classifiers starting with ["+prefix+"]="+params.size());
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
								if( NameFinder.class.isAssignableFrom(ett.getFactoryClass())){
									tagmap.put(tag, ett);
									try {
										derived.put(tag, new NamePropExpression(ett));
									} catch (PropertyCastException e) {
										log.error("Error type of expression and tag don't match",e);
									}
								}else{
									log.error("The Factory for ReferenceTag "+target_tag.getFullName()+" is not a NameFinder");
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
	@SuppressWarnings("unchecked")
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		//As we are adding fall-back definitions for properties we use
		// as input add these aa alternatives in case we need the
		// previous defn.
		for(PropertyTag key : derived.keySet()){
			
			
			PropExpression old = previous.get(key);
			try{

				if( old != null ){
					fallback.put(key, old);
				}
				
				PropExpression d = derived.get(key);
				if(old != null &&  !  d.equals(old)) {
					// Allow evaluation to falback to previous defn
					previous.put(key, new SelectPropExpression<String>(true ,String.class, derived.get(key),old));
				}else {
					previous.put(key, derived.get(key));
				}
				

			}catch(PropertyCastException e){
				getLogger().error("Error adding classification derived",e);
			}
		}
		return previous;
	}
	public final class AddClassifierAction extends FormAction {
		private DataObjectFactory target;
		public AddClassifierAction(DataObjectFactory target){
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
			//TODO consider creating a reference field if it does not exist
			return new ViewTableResult(target);
		}
	}
	public class AddClassifierTransition implements ExtraFormTransition<DataObjectFactory>{

		

		@SuppressWarnings("unchecked")
		public void buildForm(Form f, DataObjectFactory target,
				AppContext c) throws TransitionException {
			try {
			PropertyTargetGenerator fac = ExpressionCast.getPropertyTargetGenerator(target);
			SetInput<PropertyTag<String>> name_input = new SetInput<>();
			for(PropertyTag<String> t : available_names){
				// allow all names as we can classify non saved properties parser can generate
				name_input.addChoice(t);
			}
			f.addInput("Name", "String to classify", name_input);
			SetInput<ReferenceTag> ref_input = new SetInput<>();
			for(ReferenceTag t : available_refs){
				if( fac.hasProperty(t) && ! target.getTag().equals(t.getTable())){
					// Only allow supported references.
					ref_input.addChoice(t);
				}
			}
			f.addInput("Ref", "Reference to Set", ref_input);
			f.addAction("Add", new AddClassifierAction(target));
			}catch(Exception e) {
				getLogger().error("Internal error", e);
				throw new TransitionException("Internal error");
			}
		}

		public FormResult getResult(TransitionVisitor<DataObjectFactory> vis) throws TransitionException {
			return vis.doFormTransition(this);
		}

		public <X extends ContentBuilder> X getExtraHtml(X cb,
				SessionService<?> op, DataObjectFactory target) {
			cb.addText("The reference field generated by the classification has to " +
					"exist before adding the classification. You and can add additional references from the table edit page.");
			return cb;
		}
		
	}
	public final class DeleteClassifierAction extends FormAction {
		private DataObjectFactory target;
		public DeleteClassifierAction(DataObjectFactory target){
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
	public class DeleteClassifierTransition extends AbstractFormTransition<DataObjectFactory>{

	

		public void buildForm(Form f, DataObjectFactory target,
				AppContext c) throws TransitionException {
			SetInput<PropertyTag<String>> name_input = new SetInput<>();
			for(PropertyTag<String> t : tagmap.keySet()){
				name_input.addChoice(t);
			}
			f.addInput("Name", "Classifier to remove", name_input);
			f.addAction("Remove", new DeleteClassifierAction(target));
			
		}
	}
	public final class RegenerateClassifierAction  extends FormAction{
		private DataObjectFactory target;
		public RegenerateClassifierAction(DataObjectFactory target){
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
				getLogger().error("Error in Classifier regenerate",e);
				return new MessageResult("internal_error");
			}
			return new ViewTableResult(target);
		}
		
		
	}
	public class RegenerateClassifierTransition extends AbstractFormTransition<DataObjectFactory>{

		public void buildForm(Form f, DataObjectFactory target,
				AppContext conn) throws TransitionException {
			SetInput<PropertyTag<String>> name_input = new SetInput<>();
			ExpressionTargetFactory etf = ExpressionCast.getExpressionTargetFactory(target);
			if( etf != null){
				
				for(PropertyTag<String> t : tagmap.keySet()){
					name_input.addChoice(t);
				}
			}
			f.addInput("Name", "Classifier to regenerate", name_input);
			f.addAction("Regenerate", new RegenerateClassifierAction(target));	
		}
	}
	public Map<TableTransitionKey, Transition> getTableTransitions() {
		Map<TableTransitionKey,Transition> result = new HashMap<>();
		result.put(new AdminOperationKey( "AddClassifier"), new AddClassifierTransition());
		result.put(new AdminOperationKey( "RemoveClassifier"), new DeleteClassifierTransition());
		result.put(new AdminOperationKey("RegenerateClassifier","Regenenerate classifications references"),new RegenerateClassifierTransition());
		return result;
	}
	public void getTableTransitionSummary(ContentBuilder hb,
			SessionService operator) {
		hb.addText("This policy generates references to a entries in a Classifier table based"
				+" on a string property. New entries are created as required");
		try{
			Table<String,String> t = new Table<>();
			for(PropertyTag<String> name : tagmap.keySet()){
				t.put("Classifier",name.getFullName(),tagmap.get(name));
			}
			t.setKeyName("Source");
			t.sortRows();
			if( t.hasData()){
				hb.addTable(getContext(), t);
			}
		}catch(Exception e){
			getLogger().error("Error making ClassificationPolicy summary table",e);
		}
	}
	@SuppressWarnings("unchecked")
	private void regenerate(PropertyTag<String> name) throws Exception {
		DataObjectFactory fac = getContext().makeObjectWithDefault(DataObjectFactory.class, null, table);
		DatabaseService serv = getContext().getService(DatabaseService.class);
		ExpressionTargetFactory etf = ExpressionCast.getExpressionTargetFactory(fac);
		PropertyUpdater updater = new PropertyUpdater<>(fac);
		if( fac != null ){
			
				ReferenceTag ref = tagmap.get(name);
				NameFinder<? extends DataObject> nameFinder = getContext().makeObject(NameFinder.class,ref.getTable());
				Set<String> values = etf.getValues(name, null);
				
				for(String s : values ){
					DataObject o = nameFinder.makeFromString(s);
					if( o != null ){
						updater.update(ref,ref.makeReference(o),new SelectClause<>(name, s));
						serv.commitTransaction(); // Don't want the transactions to get too big
					}
				}
			
		}
	}
	@Override
	public TableSpecification modifyDefaultTableSpecification(TableSpecification t,PropExpressionMap map,String tag_name) {
	    
		if( t != null ){ 
			// We have to go straight to properties as the initFinder has not been
			// called at this point
			String prefix = CLASSIFICATION+tag_name+".";
			
			Hashtable<String,String> params = getContext().getInitParameters(prefix);
			
			ReferencePropertyRegistry reg = ReferencePropertyRegistry.getInstance(getContext());
			for(String paramName : params.values()){
				// This will only work for standard reference tags If you add custom 
				// reference tags they will need a corresponding custom table specification.
				ReferenceTag r = (ReferenceTag) reg.find(paramName);
				if( r != null ){
					String name = r.getTable()+"ID";
					if( t.goodFieldName(name)){
						t.setField(name, new ReferenceFieldType(r.getTable()));
					}else{
						getLogger().error("Bad field name "+name+" in ClassificationPolicy");
					}
				}
			}
		}
		return t;
	}
}