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

import java.util.*;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.*;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.formatters.value.ShortTextPeriodFormatter;
import uk.ac.ed.epcc.safe.accounting.properties.*;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.*;
import uk.ac.ed.epcc.safe.accounting.update.ReadOnlyParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.CurrentTimeService;
import uk.ac.ed.epcc.webapp.NumberOp;
import uk.ac.ed.epcc.webapp.content.*;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.FormValidator;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.ValidateException;
import uk.ac.ed.epcc.webapp.forms.html.RedirectResult;
import uk.ac.ed.epcc.webapp.forms.inputs.*;
import uk.ac.ed.epcc.webapp.forms.result.*;
import uk.ac.ed.epcc.webapp.forms.transition.*;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.GenericBinaryFilter;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.*;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.forms.*;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.DataObjectItemInput;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.log.Viewable;
import uk.ac.ed.epcc.webapp.model.period.GatedTransition;
import uk.ac.ed.epcc.webapp.model.period.SplitTransition;
import uk.ac.ed.epcc.webapp.preferences.Preference;
import uk.ac.ed.epcc.webapp.servlet.LoginServlet;
import uk.ac.ed.epcc.webapp.servlet.TransitionServlet;
import uk.ac.ed.epcc.webapp.servlet.ViewTransitionKey;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.session.UnknownRelationshipException;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
import uk.ac.ed.epcc.webapp.time.ViewPeriod;
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
 * @param <T> type of record
 * @param <R> intermediate record type for parse
 */


public class AllocationFactory<T extends AllocationFactory.AllocationRecord,R> extends UsageRecordFactory<T> implements
		AllocationManager<AllocationKey<T>,T>,ConfigParamProvider, FormLabelProvider {
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
			for(String name : list.split("\\s*,\\s*")){
				try{
					AllocationListener l = c.makeObject(AllocationListener.class, name);
					if( l != null ){
						result.add(l);
					}else {
						getLogger().warn("Unresolved Allocation listener "+name);
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
			super( AllocationFactory.this);
		}
		@Override
		public FormResult getResult(T dat, Form f) {
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
				supress.add(getStartField());
				supress.add(getEndField());
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
		public void postUpdate(T o, Form f,Map<String,Object> orig, boolean changed) throws DataException {
			if( changed ) {
				String diff = f.diff(orig);
				notifyModified(o,diff);
			}
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
					() -> serv.hasRole(AllocationManager.ALLOCATION_ADMIN_ROLE));
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
			Date start=(Date)f.get(getStartField());
			Date end=(Date)f.get(getEndField());
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
		@Override
		public FormResult getResult(T dat, Form f) {
			return new ChainedTransitionResult<>(AllocationFactory.this, dat, null);
		}

		
		public Map<String, Object> getCreationDefaults() {
			Map<String,Object> result = new HashMap<>();
			Map<String,Object> more = super.getCreationDefaults();
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

		@Override
		public HashMap getInitialFixtures() {
			if( defaults == null) {
				return null;
			}
			HashMap fix = new HashMap(defaults);
			fix.remove(getStartField());
			fix.remove(getEndField());
			return fix;
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
			return new InternalErrorResult();
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
		defs.put(getStartField(), p.getStart());
		defs.put(getEndField(), p.getEnd());
		def_props.setProperty(StandardProperties.STARTED_PROP, p.getStart());
		def_props.setProperty(StandardProperties.ENDED_PROP, p.getEnd());
		AccessorMap map = getAccessorMap();
		for(PropertyTag tag : defaults.propertySet()) {
			if( tag.getTarget() == IndexedReference.class) {
				IndexedReference r = (IndexedReference) defaults.getProperty(tag);
				if( r != null && ! r.isNull()) {
					String field = map.getField(tag);
					if( field != null) {
						defs.put(field, r.getID());
					}
				}
			}
		}
		
		CreateAllocation create = new CreateAllocation(defs,def_props);
		create.buildForm(f, getContext());
	}
	/** Get the correct input to use for the date fields
	 * 
	 */
	public final BoundedDateInput getDateInput(){
		int min = getMinDateField();
		if( USE_DATE_PREF.isEnabled(getContext()) && (min == Calendar.DAY_OF_YEAR || min == Calendar.DAY_OF_MONTH)) {
			return new DateInput();
		}
	
		return new TimeStampMultiInput(getContext().getService(CurrentTimeService.class).getCurrentTime(),1000L, min);
	}

	/** Get he minimum resolution {@link Calendar} field to use for date inputs
	 * 
	 * @return
	 */
	public int getMinDateField() {
		return Calendar.DAY_OF_MONTH;
	}
	
    public final Selector<BoundedDateInput> getDateSelector(){
    	return new Selector<BoundedDateInput>() {

			@Override
			public BoundedDateInput getInput() {
				return getDateInput();
			}
    		
    	};
    }
    /** Get the start field using properties.
	 * As parsers etc might create a non-standard field that the
	 * standard property is aliased to this is more robust than
	 * using the default field in StandardProperties
	 * 
	 * @return String field name
	 */
	protected String getStartField() {
		return getAccessorMap().getField(StandardProperties.STARTED_PROP);
	}
	/** Get the end field using properties.
	 * As parsers etc might create a non-standard field that the
	 * standard property is aliased to this is more robust than
	 * using the default field in StandardProperties
	 * 
	 * @return String field name
	 */
	protected String getEndField() {
		return getAccessorMap().getField(StandardProperties.ENDED_PROP);
	}
	
	@Override
	protected Map<String, Selector> getSelectors() {
		Map<String,Selector>res = super.getSelectors();
		
		Selector<BoundedDateInput> s = getDateSelector();
		res.put(getStartField(), s);
		res.put(getEndField(), s);
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
				AndFilter<X> fil = dof.getAndFilter(dof.getFinalSelectFilter());
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
		// we allow non-modifying view transitions for users without the admin roles 
		// as these should be navigation transitions that any user allowed to view thr transition can use.
		if( service.hasRoleFromList(ALLOCATION_ADMIN_ROLE,getTag()+ALLOCATION_ADMIN_ROLE) ||
				((key instanceof ViewTransitionKey) && ((ViewTransitionKey)key).isNonModifying(target))){
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
		try {
			start.setValue(c.getTime());
		} catch (TypeException e) {
			throw new TypeError(e);
		}
		
		Input<Date> end = getDateInput();
		c.add(Calendar.YEAR,1);
		try {
			end.setValue(c.getTime());
		} catch (TypeException e) {
			throw new TypeError(e);
		}
		
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
			if( writable(t) && t instanceof ReferenceTag){
				// By default we take all reference tags (from the ReferencePropertyRegistry) as part of the index.
				// We can supress this using properties but
				// also possible to override this method and remove particular tags.
				
				ReferenceTag r = (ReferenceTag) t;
				if(( ! r.getTable().equals(getTag())) && getContext().getBooleanParameter(getConfigTag()+"."+r.getTable()+".is_index", true)) {
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
				//DataObjectItemInput<?> i = (DataObjectItemInput) f.getInput(ref.getTable());
				DataObject o = (DataObject) f.getItem(ref.getTable());
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
		// Use the input to format so we only need to override
		// one method to change resolution
		BoundedDateInput input = getDateInput();
		tab.put("View", target, new Link(getContext(), "View", target.getViewTransition()));
		String start_date = START_DATE_COL;
		tab.put(start_date, target, input.getString(target.getStart()));

		Date period_start = null;
		if( template != null ){
			period_start = template.getProperty(StandardProperties.STARTED_PROP,null);
		}

		if( period_start != null && period_start.after(target.getStart())){
			tab.addAttribute(start_date, target, "class", "warn");
		}

		String end_date = END_DATE_COL;
		tab.put(end_date, target,input.getString(target.getEnd())); 

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
	
	private DataObjectLabeller<T> labeller=null; 
	protected final <X> String getLabel(PropertyTag<X> t){
		if( labeller == null) {
			labeller = new DataObjectLabeller<>(this);
		}
		String field = getAccessorMap().getField(t);
		if( field != null ){
			return labeller.getLabel(field);
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
				() -> sess.hasRole(AllocationManager.ALLOCATION_ADMIN_ROLE));
	}
	@Override
	public BaseFilter<T> getViewFilter(SessionService sess) {
		try {
			return sess.getRelationshipRoleFilter(this, AllocationManager.VIEW_ALLOCATION_RELATIONSHIP);
		} catch (UnknownRelationshipException e) {
			return new GenericBinaryFilter<>( sess.hasRole(AllocationManager.ALLOCATION_ADMIN_ROLE));
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
	public Map<String, String> addTranslations(Map<String,String> result) {
		result.put(getStartField(), getTypeName()+" Start");
		result.put(getEndField(), getTypeName()+" End");
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
			if( conn.getBooleanParameter("split_property."+getTag()+"."+p.getName(), splitByDefault())) {
				result.add(p);
			}
		}
		return result;
	}
	/** Should numeric properties be divided by default
	 * when record is split.
	 * This is the default. The behaviour of individual properties 
	 * can be customised using config settings.
	 * 
	 * @return
	 */
	protected boolean splitByDefault() {
		return true;
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
	/** return true unless {@link #notifyAggregate(AllocationRecord, PropertyContainer, boolean)} 
	 * is a null-operation
	 * 
	 * @return
	 */
	public final boolean wantNotifyAggregate() {
		return ! listeners.isEmpty();
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

	/** Is it leagal to update to the proposed values
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

	/** Get the type name to use in messages etc.
	 * 
	 * @return String
	 */
	public String getTypeName() {
		return "Allocation";
	}

	@Override
	public ViewPeriod getDefaultViewPeriod() {
		AppContext conn =getContext();
		Calendar start = Calendar.getInstance();
		CurrentTimeService time = conn.getService(CurrentTimeService.class);
		start.setTime(time.getCurrentTime());
		start.set(Calendar.MILLISECOND,0);
		start.set(Calendar.SECOND,0);
		start.set(Calendar.MINUTE,0);
		start.set(Calendar.HOUR_OF_DAY,0);
		start.set(Calendar.DAY_OF_YEAR,1);
		start.add(Calendar.YEAR, conn.getIntegerParameter("default_period.back",-1));
		return new ViewPeriod(start,Calendar.YEAR,1,conn.getIntegerParameter("default_period.length", 3));
	}
	
	
}