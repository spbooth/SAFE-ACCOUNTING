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
package uk.ac.ed.epcc.safe.accounting.allocations;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.ConfigUploadParseTargetPlugin;
import uk.ac.ed.epcc.safe.accounting.db.ProxyOwnerContainer;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.formatters.value.ShortTextPeriodFormatter;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.safe.accounting.update.ReadOnlyParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.CurrentTimeService;
import uk.ac.ed.epcc.webapp.NumberOp;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.content.Link;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.FormValidator;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.ValidateException;
import uk.ac.ed.epcc.webapp.forms.html.RedirectResult;
import uk.ac.ed.epcc.webapp.forms.inputs.BoundedDateInput;
import uk.ac.ed.epcc.webapp.forms.inputs.DateInput;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
import uk.ac.ed.epcc.webapp.forms.inputs.TimeStampMultiInput;
import uk.ac.ed.epcc.webapp.forms.result.BackResult;
import uk.ac.ed.epcc.webapp.forms.result.ChainedTransitionResult;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.MessageResult;
import uk.ac.ed.epcc.webapp.forms.result.ViewTransitionResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractDirectTransition;
import uk.ac.ed.epcc.webapp.forms.transition.ConfirmTransition;
import uk.ac.ed.epcc.webapp.forms.transition.TargetLessTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.forms.transition.TransitionFactoryVisitor;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.GenericBinaryFilter;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFormFactory;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.forms.CreateTransition;
import uk.ac.ed.epcc.webapp.model.data.forms.Selector;
import uk.ac.ed.epcc.webapp.model.data.forms.UpdateTransition;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.DataObjectItemInput;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.log.Viewable;
import uk.ac.ed.epcc.webapp.model.period.GatedTransition;
import uk.ac.ed.epcc.webapp.model.period.SplitTransition;
import uk.ac.ed.epcc.webapp.preferences.Preference;
import uk.ac.ed.epcc.webapp.servlet.LoginServlet;
import uk.ac.ed.epcc.webapp.servlet.TransitionServlet;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.session.UnknownRelationshipException;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
/** An Allocation represents a resource allocated rather than a resource consumed. 
 * This extends {@link UsageRecordFactory} and include a {@link ConfigUploadParseTargetPlugin} to allow full use of policies and to allow allocations to be parsed 
 * from an external source.
 *The main difference is that allocations also provide mechanisms for the data to be edited manually.
 *<p>
 * The funding stream is identified by a set of Index properties (references)
 * by default numerical properties are taken as allocations unless <b>allocation_properties.<i>table-tag</i>.<i>prop-name</i></b> is
 * set to false. The allocation properties also form the default set of properties to be divided when a record is split unless
 * <b>split_properties.<i>table-tag</i>.<i>prop-name</i></b> is
 * set to false.
 * <p>
 * Additional properties can be added to the record summary by setting
 * <b><i>table-tag</i>.summary_properties</b> and to the index table by setting
 * <b><i>table-tag</i>.list_properties</b>.
 * 
 * @author spb
 *
 * @param <T>
 * @param <R> intermediate record type for parse
 */


public class AllocationFactory<T extends AllocationFactory.AllocationRecord,R> extends UsageRecordFactory<T> implements
		AllocationManager<AllocationKey<T>,T>,ConfigParamProvider {
	// parse using upload method but default to ReadOnlyParser
	// so we have time fields created
	public final ConfigUploadParseTargetPlugin<T, R> parse_plugin = new ConfigUploadParseTargetPlugin<>(this,ReadOnlyParser.class);
	private static final String SUMMARY_PROPERTIES_SUFFIX = "summary_properties";
	private static final String LIST_PROPERTIES_SUFFIX = "list_properties";
	
	private static final String END_DATE_COL = "End date";
	private static final String START_DATE_COL = "Start date";
	protected static final String VALUE_COL = "Value";
	@SuppressWarnings("unchecked")
	public static final AllocationKey UPDATE = new AllocationKey(AllocationRecord.class, "Update");
	@SuppressWarnings("unchecked")
	public static final AllocationKey CREATE = new AllocationKey(
					AllocationRecord.class, "Create");
	@SuppressWarnings("unchecked")
	private static final AllocationKey DELETE = new AllocationKey(AllocationRecord.class, "Delete");
	@SuppressWarnings("unchecked")
	private static final AllocationKey SPLIT = new AllocationKey(AllocationRecord.class, "Split", "Split a record at a given date");
	
	public static final Preference USE_DATE_PREF = new Preference("allocation.use_date_input", true, "use html5 date input for allocation forms");

	private static final String ALLOCATION = "Allocation";
	public static class AllocationRecord extends UsageRecordFactory.Use implements Viewable, Allocation, ProxyOwnerContainer{

		
		protected AllocationRecord(AllocationFactory fac, Record r) {
			super(fac, r);
			
		}

		

		@SuppressWarnings("unchecked")
		public ViewTransitionResult getViewTransition() {
			return new ViewTransitionResult((AllocationFactory)getFactory(), this);
		}




		
	}
	
	private Map<AllocationKey<T>, Transition<T>> transitions; 
	private Set<AllocationListener<T>> listeners;
	@Override
	public Class<T> getTarget() {
		return (Class<T>) AllocationRecord.class;
	}

	@Override
	protected T makeBDO(Record res) throws DataFault {
		return (T) new AllocationRecord(this, res);
	}
	public final ExpressionTargetFactory<T> getExpressionTargetFactory(){
		return ExpressionCast.getExpressionTargetFactory(this);
	}
	public final AccessorMap<T> getAccessorMap(){
		return getExpressionTargetFactory().getAccessorMap();
	}
	
	public AllocationFactory(AppContext c, String table){
		super();
		setContext(c,table);
		transitions=makeTransitions();
		listeners=makeListeners(c, table);
	}
	public Set<AllocationListener<T>> makeListeners(AppContext c,String table){
		Set<AllocationListener<T>> result = new LinkedHashSet<>();
		String list = c.getInitParameter(table+".allocation_listeners");
		if( list != null ){
			for(String name : list.split("\\s,\\s")){
				try{
					AllocationListener l = c.makeObject(AllocationListener.class, name);
					if( l != null ){
						result.add(l);
					}
				}catch(Exception t){
					getLogger().error("Error making allocation listener "+name, t);
				}
			}
		}
		return result;
	}

	public class ListenerUpdateValidator implements FormValidator{

		public ListenerUpdateValidator(T orig) {
			super();
			this.orig = orig;
		}

		private final T orig;
		
		public void validate(Form f) throws ValidateException {
			if( listeners == null ){
				return;
			}
			PropertyMap map = new PropertyMap();
			map.setAll(orig);
			try {
				getAccessorMap().addFormContents(map, f);
			} catch (InvalidPropertyException e) {
				getLogger().error("Error reading form",e);
				return;
			}
			canModify(orig,map);
			
		}
		
	}
	/** Transition to edit Allocation records
	 * 
	 * @author spb
	 *
	 */
	public class AllocationEdit extends UpdateTransition<T> implements GatedTransition<T>{
		
		protected AllocationEdit() {
			super(ALLOCATION, AllocationFactory.this);
		}

		public FormResult getResult(String typeName, T dat, Form f) {
			return new ChainedTransitionResult<>(AllocationFactory.this, dat, null);
		}

		@Override
		protected Set<String> getSupress() {
			// Don't edit index properties.
			Set<String> supress = super.getSupress();
			if( supress == null){
				supress = new HashSet<>();
			}
			if( ! editEnds()){
				supress.add(StandardProperties.STARTED_TIMESTAMP);
				supress.add(StandardProperties.COMPLETED_TIMESTAMP);
			}
			AccessorMap<T> map = getAccessorMap();
			
			for(PropertyTag t : getIndexProperties() ){
				String field = map.getField(t);
				if( field != null ){
					supress.add(field);
				}
			}
			return supress;
		}

		@Override
		public void postUpdate(T o, Form f,Map<String,Object> orig) throws DataException {
			String diff = f.diff(orig);
			notifyModified(o,diff);
		}

		@Override
		public void customiseUpdateForm(Form f, T o) {
			if( editEnds()){
				f.addValidator(new AllocationValidator());
			}
			f.addValidator(new ListenerUpdateValidator(o));
			addUpdateValidator(f, o);
			modifyAllocationForm(f, o);
		}

		@Override
		public boolean allow(SessionService<?> serv, T target) {
			return serv.hasRelationship(AllocationFactory.this, target, 
					AllocationManager.EDIT_ALLOCATION_RELATIONSHIP, 
					serv.hasRole(AllocationManager.ALLOCATION_ADMIN_ROLE));
		}

		/* (non-Javadoc)
		 * @see uk.ac.ed.epcc.webapp.model.data.DataObjectFormFactory#getSelectors()
		 */
		
		
	}
	/** extension point to allow customisation of the allocation form.
	 * 
	 * @param f
	 * @param o
	 */
	protected void modifyAllocationForm(Form f, T o) {
		
	}
	public class AllocationValidator implements FormValidator{
		public void validate(Form f) throws  ValidateException{
			Date start=(Date)f.get(StandardProperties.STARTED_TIMESTAMP);
			Date end=(Date)f.get(StandardProperties.COMPLETED_TIMESTAMP);
			if( ! end.after(start) ){
				throw new ValidateException("End not after Start");
			}
		}
	}
	protected void addCreationValidator(Form f){
		f.addValidator(new AllocationValidator());
	}
	protected void addUpdateValidator(Form f, T o){
		
	}
	public class ListenerCreateValidator implements FormValidator{
		public ListenerCreateValidator(PropertyContainer def_props) {
			super();
			this.def_props = def_props;
		}
		private final PropertyContainer def_props;
		public void validate(Form f) throws ValidateException {
			if( listeners == null ){
				return;
			}
			PropertyMap map = new PropertyMap();
			map.setAll(def_props);
			try {
				getAccessorMap().addFormContents(map, f);
			} catch (InvalidPropertyException e) {
				getLogger().error("Error reading form",e);
				return;
			}
			checkCreate(map);
			
		}
		
	}
	/** Transition to create allocation records
	 * 
	 * @author spb
	 *
	 */
	public class CreateAllocation extends CreateTransition<T>{
		

		
		Map<String,Object> defaults;
		PropertyContainer default_props;
		protected CreateAllocation(){
			this(new HashMap<String, Object>(), new PropertyMap());
		}
		
		protected CreateAllocation(Map<String,Object> defaults,PropertyContainer default_props){
			super(ALLOCATION, AllocationFactory.this);
			this.defaults=defaults;
			this.default_props=default_props;
		}
		@Override
		public void customiseCreationForm(Form f) throws Exception {
			// basic validator first
			addCreationValidator(f);
			f.addValidator(new ListenerCreateValidator(default_props));
		}
		public FormResult getResult(String type_name, T dat, Form f) {
			return new ChainedTransitionResult<>(AllocationFactory.this, dat, null);
		}

		
		public Map<String, Object> getDefaults() {
			Map<String,Object> result = new HashMap<>();
			Map<String,Object> more = super.getDefaults();
			if( more != null ){
				result.putAll(more);
			}
			result.putAll(defaults);
			return result;
		}
		public void postCreate(T dat, Form f) throws Exception {
			super.postCreate(dat, f);
			notifyCreated(dat);
			
		}
	}
	public class AllocationDelete extends AbstractDirectTransition<T> {

		public FormResult doTransition(T target, AppContext c)
				throws TransitionException {
			try {
				notifyDeleted(target);
				if( target.delete()){
					
					return new BackResult(getTransitionProvider(), new RedirectResult(LoginServlet.getMainPage(getContext())));
				}
			} catch (DataFault e) {
				getLogger().error("Error deleting Allocation",e);
			}
			return new MessageResult("internal_error");
		}
		
	}
	public class AllocationView extends AbstractDirectTransition<T>{


		public FormResult doTransition(T target, AppContext c)
				throws TransitionException {
			return new ViewTransitionResult<>(AllocationFactory.this, target);
		}
		
	}
	public static final AllocationKey INDEX = new AllocationKey(AllocationRecord.class, "Index","Return to index page");
	public class ReturnIndexTransition extends AbstractDirectTransition<T>implements GatedTransition<T>{

		public boolean allow(SessionService<?> serv, T target) {
			return TransitionServlet.hasViewRecorded(serv, getTransitionProvider());
		}

		public FormResult doTransition(T target, AppContext c)
				throws TransitionException {
		
			return new BackResult(getTransitionProvider(), new RedirectResult(LoginServlet.getMainPage(getContext())));
		}
		
	}
	public AllocationPeriodTransitionProvider getTransitionProvider(){
		return new AllocationPeriodTransitionProvider(this);
	}
	
	@SuppressWarnings("unchecked")
	protected LinkedHashMap<AllocationKey<T>, Transition<T>> makeTransitions() {
		LinkedHashMap<AllocationKey<T>,Transition<T>> result = new LinkedHashMap<>();
		if( canSplit()) {
			result.put(SPLIT, new SplitTransition<>(this, this));
		}
		result.put(CREATE, new CreateAllocation());
		result.put(DELETE, new ConfirmTransition<>("Are you sure you want to delete this allocation?", new AllocationDelete(), new AllocationView()));
		result.put(INDEX, new ReturnIndexTransition());
		result.put(UPDATE, new AllocationEdit());
		return result;
	}

	public void buildCreationForm(Form f, Period p, PropertyMap defaults) throws TransitionException{
		PropertyMap def_props = new PropertyMap();
		def_props.setAll(defaults);
		Map<String,Object> defs = new HashMap<>();
		defs.put(StandardProperties.STARTED_TIMESTAMP, p.getStart());
		defs.put(StandardProperties.COMPLETED_TIMESTAMP, p.getEnd());
		def_props.setProperty(StandardProperties.STARTED_PROP, p.getStart());
		def_props.setProperty(StandardProperties.ENDED_PROP, p.getEnd());
		AccessorMap map = getAccessorMap();
		for(ReferenceTag<?,?> tag : getIndexProperties()){
			String field = map.getField(tag);
			IndexedReference ref = defaults.getProperty(tag, null);
			if( field != null && ref != null){
				defs.put(field,ref.getID());
			}
		}
		CreateAllocation create = new CreateAllocation(defs,def_props);
		create.buildForm(f, getContext());
	}
	/** Get the correct input to use for the date fields
	 * 
	 */
	public final BoundedDateInput getDateInput(){
		if( USE_DATE_PREF.isEnabled(getContext())) {
			return new DateInput();
		}
		return new TimeStampMultiInput(getContext().getService(CurrentTimeService.class).getCurrentTime(),1000L, Calendar.DAY_OF_MONTH);
	}
	
    public final Selector<BoundedDateInput> getDateSelector(){
    	return new Selector<BoundedDateInput>() {

			@Override
			public BoundedDateInput getInput() {
				return getDateInput();
			}
    		
    	};
    }
	
	
	
	@Override
	protected Map<String, Selector> getSelectors() {
		Map<String,Selector>res = super.getSelectors();
		
		Selector<BoundedDateInput> s = getDateSelector();
		res.put(StandardProperties.STARTED_TIMESTAMP, s);
		res.put(StandardProperties.COMPLETED_TIMESTAMP, s);
		AccessorMap map = getAccessorMap();
		SessionService sess = getContext().getService(SessionService.class);
		// Restrict index properties to those we have a particular role on
		for(ReferenceTag tag : getIndexProperties()){
			String field = map.getField(tag);
			if( field != null ){
				IndexedProducer prod = tag.getFactory(getContext());
				if( prod instanceof DataObjectFactory){
					DataObjectFactory dof = (DataObjectFactory) prod;
					res.put(field, dof.getSelector(sess.getRelationshipRoleFilter(dof, AllocationPeriodTransitionProvider.ALLOCATION_ADMIN_RELATIONSHIP, dof.getFinalSelectFilter())));
					
				}
			}
		}
		return res;
	}

	/** Get a form input from a reference tag using the normal
	 * access control restrictions
	 * 
	 * @param tag
	 * @param narrow
	 * @return
	 */
	public <X extends DataObject> DataObjectItemInput<X> getPropertyInput(ReferenceTag<X, ? extends DataObjectFactory<X>> tag, BaseFilter<X> narrow){
		AccessorMap map = getAccessorMap();
		SessionService sess = getContext().getService(SessionService.class);
		String field = map.getField(tag);
		if( field != null ){
			IndexedProducer prod = tag.getFactory(getContext());
			if( prod instanceof DataObjectFactory){
				DataObjectFactory<X> dof = (DataObjectFactory) prod;
				AndFilter<X> fil = new AndFilter<>(dof.getTarget(),dof.getFinalSelectFilter());
				if( narrow != null) {
					fil.addFilter(narrow);
				}
				return dof.getInput(sess.getRelationshipRoleFilter(dof, AllocationPeriodTransitionProvider.ALLOCATION_ADMIN_RELATIONSHIP, fil));				
			}
		}
		return null;
	}
	public String getTargetName() {
		return getTag();
	}

	
	
	
	@SuppressWarnings("unchecked")
	public boolean allowTransition(AppContext c, T target, AllocationKey<T> key) {
		SessionService service = c.getService(SessionService.class);
		if( service.hasRoleFromList(ALLOCATION_ADMIN_ROLE,getTag()+ALLOCATION_ADMIN_ROLE)){
			Transition<T> t = getTransition(target, key);
			if( t != null){
				if( t instanceof TargetLessTransition){
					return target == null;
				}else if( t instanceof GatedTransition && target != null){
					return ((GatedTransition<T>)t).allow(service, target);
				}else{
					return target != null;
				}
			}
		}
		return false;
	}

	public String getID(T target) {
		return Integer.toString(target.getID());
	}

	/** get the set of properties to be show in the summary table
	 * 
	 * @return
	 */
	protected Set<PropertyTag> getSummaryProperties(){
		LinkedHashSet<PropertyTag> result = new LinkedHashSet<>();
		result.addAll(getIndexProperties());
		result.addAll(getAllocationProperties());
		result.addAll(getConfigPropertyList(SUMMARY_PROPERTIES_SUFFIX));
		return result;
	}
	public <X extends ContentBuilder> X getSummaryContent(AppContext c,X hb, T target) {
		Table<String, String> tab = getSummaryTable(target);
		hb.addColumn(getContext(), tab, VALUE_COL);
		return hb;
	}

	protected Table<String, String> getSummaryTable(T target) {
		Table<String,String> tab = new Table<>();
		ShortTextPeriodFormatter fmt = new ShortTextPeriodFormatter();
		tab.put(VALUE_COL, "Period", fmt.format(target));
		for(PropertyTag<?> t : getSummaryProperties()){
			addValueToTable(target, tab,VALUE_COL,getLabel(t), t);
		}
		tab.setKeyName("Property");
		return tab;
	}

	

	public T getTarget(String id) {
		if( id == null || id.trim().length() ==0){
			return null;
		}
		try{
			return find(Integer.parseInt(id));
		}catch(Exception t){
			return null;
		}
	}

	public Transition<T> getTransition(T target, AllocationKey<T> key) {
		return transitions.get(key);
	}

	public Set<AllocationKey<T>> getTransitions(T target) {
		LinkedHashSet<AllocationKey<T>> result = new LinkedHashSet<>();
		AppContext context = getContext();
		
		for(AllocationKey<T> k : transitions.keySet()){
			if( allowTransition(context, target, k)){
				result.add(k);
			}
		}
		return result;
	}

	public AllocationKey<T> lookupTransition(T target, String name) {
		for(AllocationKey<T> key : getTransitions(target)){
			if( key.getName().equals(name)){
				return key;
			}
		}
		return null;
	}

	/** build a form to generate a filtered view of allocations
	 * 
	 */
	public void buildFilterForm(Form filter_form) {
		Input<Date> start = getDateInput();
		AppContext conn = getContext();
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.HOUR,0);
		c.set(Calendar.DAY_OF_YEAR,1);
		start.setValue(c.getTime());
		
		Input<Date> end = getDateInput();
		c.add(Calendar.YEAR,1);
		end.setValue(c.getTime());
		
		filter_form.addInput("StartDate","Start Date", start );
		filter_form.addInput("EndDate","End Date",end );
		AccessorMap<T> map =getAccessorMap();
		// If we have any references then allow filter on these.
		for(PropertyTag<?> tag : getIndexProperties()){
			// only filter if actually present
			if( tag instanceof ReferenceTag && map.hasProperty(tag)){
				ReferenceTag ref = (ReferenceTag) tag;
				IndexedProducer prod = ref.getFactory(conn);
				if( prod instanceof Selector){
					Selector sel = (Selector)  prod;
					Input i = sel.getInput();
					filter_form.addInput(ref.getTable(), ref.getTable(), i);
					filter_form.getField(ref.getTable()).setOptional(true);
				}
			}
		}
		filter_form.addValidator(new FormValidator(){
			public void validate(Form f) throws  ValidateException{
				Date start=(Date)f.get("StartDate");
				Date end=(Date)f.get("EndDate");
				if( ! end.after(start) ){
					throw new ValidateException("End not after Start");
				}
			}
		});
	}

	
	private Set<ReferenceTag> index_properties=null;
	/** get the set of properties used to classify allocations
	 * 
	 * @return
	 */
	public final Set<ReferenceTag> getIndexProperties() {
		if( index_properties == null ){
			index_properties=makeIndexProperties();
		}
		return index_properties;
	}
	protected  Set<ReferenceTag> makeIndexProperties(){
		HashSet<ReferenceTag> result = new HashSet<>();
		for( PropertyTag<?> t : ReferencePropertyRegistry.getInstance(getContext()).getProperties()){
			if( hasProperty(t) && t instanceof ReferenceTag){
				
				ReferenceTag r = (ReferenceTag) t;
				if( ! r.getTable().equals(getTag())) {
					// Self reference tags are never part of the index
					result.add(r);
				}
			}
		}
		return result;
	}
	private Set<PropertyTag<? extends Number>> allocation_properties=null;
	/** get the set of properties used as allocations.
	 * 
	 * @return
	 */
	protected final  Set<PropertyTag<? extends Number>> getAllocationProperties() {
		if( allocation_properties == null ){
			allocation_properties=Collections.unmodifiableSet(makeAllocationProperties());
		}
		return allocation_properties;
	}
	protected Set<PropertyTag<? extends Number>> makeAllocationProperties() {
		AccessorMap<T> map = getAccessorMap();
		Set<PropertyTag<? extends Number>> result = getFinder().getProperties(Number.class);
		Iterator<PropertyTag<? extends Number>> it = result.iterator();
		while( it.hasNext()){
			PropertyTag<? extends Number> p = it.next();
			if( ! map.writable(p) || ! getContext().getBooleanParameter("allocation_property."+getTag()+"."+p.getName(), true)){
				it.remove();
			}
		}
		return result;
	}
	/** get the set of properties shown in the index list
	 * 
	 * @return
	 */
	protected Set<PropertyTag> getListProperties(){
		Set<PropertyTag> result = new LinkedHashSet<>();
		for(ReferenceTag<?,?> t : getIndexProperties()){
			if( hasProperty(t)){
				result.add(t);
			}
		}
		for(PropertyTag<?> t : getAllocationProperties()){
			if( hasProperty(t) ){
				result.add(t);
			}
		}
		result.addAll(getConfigPropertyList(LIST_PROPERTIES_SUFFIX));
		return result;
	}
	/** parse the filter form to generate a corresponding selector
	 * 
	 */
	@SuppressWarnings("unchecked")
	public RecordSelector getFilterSelector(Form f) {
		Date start_date=(Date) f.get("StartDate");
		Date end_date =(Date) f.get("EndDate");
		AndRecordSelector sel = new AndRecordSelector();
		sel.add(new PeriodOverlapRecordSelector(new Period(start_date,end_date), StandardProperties.STARTED_PROP,StandardProperties.ENDED_PROP));
		AccessorMap map = getAccessorMap();
		for(PropertyTag tag : getIndexProperties()){
			if( tag instanceof ReferenceTag && map.hasProperty(tag)){
				ReferenceTag ref = (ReferenceTag) tag;
				DataObjectItemInput<?> i = (DataObjectItemInput) f.getInput(ref.getTable());
				DataObject o = i.getItem();
				if( o != null ){
					sel.add(new SelectClause<IndexedReference>(ref,ref.makeReference(o)));
				}
			}
		}
		return sel;
	}
	public RecordSelector getOverlapSelector(TimePeriod p, PropertyContainer cont) throws InvalidExpressionException {
		AndRecordSelector sel = new AndRecordSelector();
		sel.add(new PeriodOverlapRecordSelector(p, StandardProperties.STARTED_PROP,StandardProperties.ENDED_PROP));
		for(PropertyTag tag : getIndexProperties()){
			if( cont.getProperty(tag, null) != null ){
				sel.add(new SelectClause(tag,cont));
			}
		}
		return sel;
	}

	
	public void finishIndexTable(Table<String,T> tab, PropertyTarget template) {
		LinkedList<String> order = new LinkedList<>();
		
		for(PropertyTag tag : getIndexProperties()){
			if( template == null || template.getProperty(tag, null)==null){
				order.add(getLabel(tag));
			}
		}
		order.add(START_DATE_COL);
		tab.sortRows(order.toArray(new String[order.size()]), false);
	}
		
	
	@SuppressWarnings("unchecked")
	public   Table<String,T> addIndexTable(Table<String,T> tab, T target, PropertyTarget template) {
		Date now = getContext().getService(CurrentTimeService.class).getCurrentTime();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		tab.put("View", target, new Link(getContext(), "View", target.getViewTransition()));
		String start_date = START_DATE_COL;
		tab.put(start_date, target, df.format(target.getStart()));

		Date period_start = null;
		if( template != null ){
			period_start = template.getProperty(StandardProperties.STARTED_PROP,null);
		}

		if( period_start != null && period_start.after(target.getStart())){
			tab.addAttribute(start_date, target, "class", "warn");
		}

		String end_date = END_DATE_COL;
		tab.put(end_date, target,df.format(target.getEnd())); 

		Date period_end = null;
		if( template != null ){
			period_end = template.getProperty(StandardProperties.ENDED_PROP,null);
		}

		if( period_end != null ) {
			if( period_end.before(target.getEnd())){
				tab.addAttribute(end_date, target, "class", "warn");
			}else if( target.getEnd().before(now)) {
				tab.addAttribute(end_date, target, "class", "grey");
			}
		}

		for(PropertyTag tag : getListProperties()){
			if( template == null || template.getProperty(tag, null)==null){
				addValueToTable(target, tab,getLabel(tag),target, tag);			
			}
		}
		
		return tab;
	}

	protected final <X> String getLabel(PropertyTag<X> t){
		String field = getAccessorMap().getField(t);
		if( field != null ){
			String label = getTranslations().get(field);
			if( label != null ){
				return label;
			}
		}
		return t.getName();
	}
	
	
	@SuppressWarnings("unchecked")
	protected  <X,C,R> void addValueToTable(T target,Table<C,R> tab,C col, R row,
			PropertyTag<X> t) {
		try{
		        Logger log = getContext().getService(LoggerService.class).getLogger(getClass());
				X val = target.getProperty(t, null);
				if( val != null ){
					Labeller lab = null;
					if( t instanceof FormatProvider){
						lab=((FormatProvider)t).getLabeller();
					}
					if( lab != null ){
						tab.put(col,row,lab.getLabel(getContext(), val));
					}else{
						Input i=null;
						String field = getAccessorMap().getField(t);
						if( field != null ){
							log.debug("Field name is"+field);
							Selector sel = getSelectors().get(field);
							if( sel != null ) {
								i = sel.getInput();
							}
							// try a property override
							if( i == null ) {
								i = DataObjectFormFactory.getInputFromName(getContext(), getConfigTag(), field);
							}
							// finally from DB type
							if( i == null ) {
								i = DataObjectFormFactory.getInputFromType(getContext(), res, res.getInfo(field));
							}
						}
						if( i != null ){
							log.debug("input type is"+i.getClass().getCanonicalName());
							tab.put(col,row,i.getPrettyString(i.convert(val)));
						}else{
							tab.put(col,row, val.toString());
						}
					}
				}
		}catch(Exception tr){
			getLogger().error("Error formating property as HTML",tr);
		}
	}

	
	public boolean canView(T target, SessionService<?> sess) {
		return sess.hasRelationship(this, target, 
				AllocationManager.VIEW_ALLOCATION_RELATIONSHIP,
				sess.hasRole(AllocationManager.ALLOCATION_ADMIN_ROLE));
	}
	@Override
	public BaseFilter<T> getViewFilter(SessionService sess) {
		try {
			return sess.getRelationshipRoleFilter(this, AllocationManager.VIEW_ALLOCATION_RELATIONSHIP);
		} catch (UnknownRelationshipException e) {
			return new GenericBinaryFilter<>(getTarget(), sess.hasRole(AllocationManager.ALLOCATION_ADMIN_ROLE));
		}
	}

	public <X extends ContentBuilder> X getLogContent(X cb,T target, SessionService<?> sess) {
		
		return getSummaryContent(getContext(),cb, target);
	}

	public String getHelp(AllocationKey<T> key) {
		return key.getHelp();
	}
	public String getText(AllocationKey<T> key) {
		return key.toString();
	}
	@Override
	protected Set<String> getOptional() {
		// require all fields in allocations
		return new HashSet<>();
	}

	

	public <X extends ContentBuilder> X getTopContent(X cb, T target,
			SessionService<?> sess) {
		return cb;
	}
	public <X extends ContentBuilder> X getBottomContent(X cb, T target,
			SessionService<?> sess) {
		return cb;
	}
	public <R> R accept(TransitionFactoryVisitor<R,T, AllocationKey<T>> vis) {
		return vis.visitTransitionProvider(this);
		
	}

	@Override
	protected Map<String, String> getTranslations() {
		Map<String,String> result = new HashMap<>();
		result.put(StandardProperties.STARTED_TIMESTAMP, "Allocation Start");
		result.put(StandardProperties.COMPLETED_TIMESTAMP, "Allocation End");
		return result;
	}
	private Set<PropertyTag<? extends Number>> split_properties = null;
	/** get the set of properties that need to be divided on a record split
	 * 
	 * @return
	 */
	protected final Set<PropertyTag<? extends Number>> getSplitProperties(){
		if( split_properties == null) {
			split_properties=Collections.unmodifiableSet(makeSplitProperties());
		}
		return split_properties;
	}

	protected  Set<PropertyTag<? extends Number>> makeSplitProperties(){
		Set<PropertyTag<? extends Number>> result = new LinkedHashSet<>();
		AppContext conn = getContext();
		for(PropertyTag<? extends Number> p : getAllocationProperties()) {
			if( conn.getBooleanParameter("split_property."+getTag()+"."+p.getName(), true)) {
				result.add(p);
			}
		}
		return result;
	}
	/** perform per property rounding in a sub-class
	 * 
	 * @param tag
	 * @param value
	 * @return
	 */
	protected  Number roundSplitValue(PropertyTag tag,Number total, Number value){
		return value;
	}
	public final T split(T orig, Date d) throws Exception {
		Date start=orig.getStart();
		Date end = orig.getEnd();
		if( d.after(end)){
			return null;
		}
		if( ! d.after(start)){
			return null;
		}
		if( d.equals(end)){
			return orig;
		}
		double frac = (double)(d.getTime()-start.getTime())/((double) end.getTime()-start.getTime());
		T peer = makeBDO();
		// start with a duplicate
		peer.setContents(orig.getMap());
		for(PropertyTag tag : getSplitProperties()){
			Number value = (Number) orig.getProperty(tag, null);
			if( value != null){
				// Scale allocation values by size of split
				Number reduced = roundSplitValue(tag,value,NumberOp.mult(value, frac));
				Number left = NumberOp.sub(value, reduced);
				// We should really take account of any accumulated use value
				
				orig.setProperty(tag, reduced);
				peer.setProperty(tag, left);
			}
		}
		peer.setProperty(StandardProperties.STARTED_PROP, d);
		orig.setProperty(StandardProperties.ENDED_PROP, d);
		orig.commit();
		peer.commit();
		notifySplit(orig, peer);
		return peer;
	}

	public Date getEditMarker() {
		CurrentTimeService time = getContext().getService(CurrentTimeService.class);
		return time.getCurrentTime();
	}

	protected boolean editEnds(){
		return true;
	}
	public void notifyCreated(T rec){
		for(AllocationListener<T> l : listeners){
			l.created(rec);
		}
	}
	public void notifyDeleted(T rec){
		for(AllocationListener<T> l : listeners){
			l.deleted(rec);
		}
	}
	public void notifyModified(T rec,String details){
		for(AllocationListener<T> l : listeners){
			l.modified(rec,details);
		}
	}
	public void notifySplit(T first,T second){
		for(AllocationListener<T> l : listeners){
			l.split(first, second);
		}
	}
	public void notifyMerge(T first,T second){
		for(AllocationListener<T> l : listeners){
			l.merge(first, second);
		}
	}
	/** Method to track charging in sub-classes
	 * @param rec  allocation being updated
	 * @param props properties of accounting record
	 * @param add addition or removal
	 * 
	 */
	public final  void notifyAggregate(T rec, PropertyContainer props, boolean add){
		for(AllocationListener<T> l : listeners){
			l.aggregate(rec, props, add);
		}
	}
	
	
	public boolean canMerge(T first, T last){
		if( first.equals(last)) {
			return false;
		}
		for(AllocationListener<T> l : listeners){
			try {
				l.canMerge(first, last);
			} catch (ListenerObjection e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param map
	 * @throws ListenerObjection
	 */
	protected void canModify(T orig,PropertyMap map) throws ListenerObjection {
		for(AllocationListener<T> l : listeners){
			
			l.canModify(orig, map);
			
		}
	}

	/** Check a proposed creation against the listeners
	 * @param map
	 * @throws ListenerObjection
	 */
	protected void checkCreate(PropertyContainer map) throws ListenerObjection {
		for(AllocationListener<T> l : listeners){
			
			l.canCreate( map);
			
		}
	}

	public boolean canSplit() {
		return true;
	}
	public void canSplit(T orig, Date d) throws ValidateException {
		
		
	}
	/** Get a set of supported {@link PropertyTag}s defined in the
	 * configuration property <b><i>table-tag</i>.<i>suffix</i></b>
	 * 
	 * @param suffix Property name suffix
	 * @return
	 */
	protected Set<PropertyTag> getConfigPropertyList(String suffix){
		Set<PropertyTag> result = new LinkedHashSet<>();
		String prop_name = getTag()+"."+suffix;
		String list = getContext().getExpandedProperty(prop_name);
		PropertyFinder finder = getFinder();
		
		if( list != null ) {
			for(String name : list.split("\\s*,\\s*")) {
				PropertyTag tag = finder.find(name);
				if( tag != null && hasProperty(tag)) {
					result.add(tag);
				}
			}
		}
		return result;
	}

	@Override
	public void addConfigParameters(Set<String> params) {
		params.add(getTag()+"."+SUMMARY_PROPERTIES_SUFFIX);
		params.add(getTag()+"."+LIST_PROPERTIES_SUFFIX);
	}

	
	
	
}