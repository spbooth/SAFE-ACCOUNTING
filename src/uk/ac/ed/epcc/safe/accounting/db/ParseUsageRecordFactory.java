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
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.NullSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.SimplePeriodInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.MessageResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.ExtraFormTransition;
import uk.ac.ed.epcc.webapp.jdbc.table.DataBaseHandlerService;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TransitionSource;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.transition.TransitionKey;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.Period;

/** Base class for UsageProducers that directly parse input.
 * 
 * Most of the specific behaviour is delegated to 
 * UsageRecordParser, UsageRecordPolicy objects which need to be defiend by sub-classes
 * 
 * By default this class will populate the AccessorMap by looking for database field names that match
 * PropertyTag names. This can be customised by setting a property
 * <pre>
 * accounting.[table-name].[field-name]=[property-name]
 * </pre>
 * The UsageRecordParser and UsageRecordPolicy objects are also given the ability 
 * define derived properties as simple expressions of other properties.
 * 
 * 
 * @author spb
 * @param <T> class of UsageRecord
 *
 */

public abstract class ParseUsageRecordFactory<T extends UsageRecordFactory.Use> extends UsageRecordFactory<T> implements UsageRecordParseTarget<T>, PlugInOwner {

	 
	
	  private  PropertyFinder property_finder=null;
	 
	  private  AccessorMap<T> mapi=null;
	 
	  protected static final String TEXT="Text"; // raw text of line
	protected ParseUsageRecordFactory(AppContext c, String table) {
		super(c, table);
	}
	
    	
	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c,
			String table) {
		// The parser can specify a default table to create.
		// Don't use anything that needs getContext as this does not work unless
		// factory is valid.
		PlugInOwner owner = makePlugInOwner(c,null, table);

		PropertyContainerParser parser=owner.getParser();
		if( parser == null ){
			// If we explicitly don't have a parser then we don't want auto-table creation.
			
			return null;
		}
		PropExpressionMap map = new PropExpressionMap();
		map = parser.getDerivedProperties(map);
		for(PropertyContainerPolicy pol : owner.getPolicies()){
			map = pol.getDerivedProperties(map);
		}
		TableSpecification spec = parser.modifyDefaultTableSpecification(c,makeInitialSpecification(),map,getConfigTag());
		// If the parser explicitly returns no spec then no table is created.
		// This will also happen if there are no AutoTable annotatiosn so no fields are added by the parser
		// You can avoid this in sub-classes by creating fields in makeInitialSpecification
		if( spec != null ){
			for(PropertyContainerPolicy pol : owner.getPolicies()){
				spec = pol.modifyDefaultTableSpecification(c,spec,map,table);
			}
			
			spec.setOptionalField(TEXT, new StringFieldType(true, null, 512));
		}
		return spec;
	}


	/** Make the initial specification that will then be modified by the parsers and policies. 
	 * This gives an extension point for sub-classes to create fields outwith the parser.
	 * 
	 * normally if the parser does not generate any fields then table creation is supressed. 
	 *
	 * 
	 * @return
	 */
	protected TableSpecification makeInitialSpecification() {
		return new TableSpecification();
	}
	
	/** bootstrap the table.
	 * This is called explicitly be the create accounting table action even if 
	 * auto_create tables is off.
	 * 
	 * @param c
	 * @param table
	 * @param spec
	 * @throws DataFault
	 */
	public static  void bootstrapTable(AppContext c, String table,
			TableSpecification spec) throws DataFault {

		if( spec == null ){
			Logger log = c.getService(LoggerService.class).getLogger(ParseUsageRecordFactory.class);
			log.error("No table specificaiton in bootstrapTable");
			return;
		}
		DataBaseHandlerService dbh = c.getService(DataBaseHandlerService.class);
		if( dbh == null ){
			return;
		}
		dbh.createTable(table, spec);
	}
	

    private boolean in_init=false;
	private final void initAccessorMap(AppContext c, String table) {
		//Logger log = c.getService(LoggerService.class).getLogger(getClass());
		if( in_init == true ){
			throw new ConsistencyError("recursive call to initAccessorMap");
		}
		in_init=true;
		try{
		MultiFinder finder = new MultiFinder();
		PropExpressionMap derived = new PropExpressionMap();
		mapi=new AccessorMap<T>(getTarget(),res,table);
		for(AccessorContributer contrib : getComposites(AccessorContributer.class)){
			contrib.customAccessors(mapi, finder, derived);
		}
		customAccessors(mapi,finder);
		
		ReferencePropertyRegistry ref_registry = ReferencePropertyRegistry.getInstance(c);
		mapi.makeReferences(ref_registry);
		finder.addFinder(ref_registry);
		
		plugin_owner=makePlugInOwner(c, ref_registry, table);
		
		finder.addFinder(plugin_owner.getFinder());
		derived.getAllFrom(plugin_owner.getDerivedProperties());
		log.debug("initAccessorMap("+table+") plugin_owner derived size="+derived.size());
		
		getParser();
		
		
		
		PropertyRegistry table_reg = new PropertyRegistry(table, "Fields from table "+table);
		mapi.populate(finder, table_reg, false);
		finder.addFinder(table_reg);
		
		property_finder=finder;
		mapi.addDerived(getContext(),derived);
		
		}finally{
			in_init=false;
		}
	}
	
	  

	/** Extension point to allow custom Accessors and the corresponding PropertyFinders to be added.
	 * Derived properties can be added directly to the  
	 * @param mapi2 AccessorMap modified 
	 * @param finder MultiFinder modified
	 */
	protected void customAccessors(AccessorMap<T> mapi2, MultiFinder finder) {
		
	}

	public final AccessorMap<T> getAccessorMap(){
		if( mapi == null ){
			initAccessorMap(getContext(), getConfigTag());
		}
		return mapi;
	}
	public PropertyFinder getFinder(){
		if(property_finder == null ){
			initAccessorMap(getContext(), getConfigTag());
		}
		return property_finder;
	}
	
	private  UsageRecordParseTarget<T> parse_target=null;
	private final UsageRecordParseTarget<T> getParseTarget(){
		if( parse_target == null ){
			parse_target = makeParseTarget(getPlugInOwner());
		}
		return parse_target;
	}
	protected UsageRecordParseTarget<T> makeParseTarget(PlugInOwner owner){
		return new UsageRecordParseTargetPlugIn<T>(getContext(), owner, this);
	}
	public T findDuplicate(T r)throws Exception {
		// Note this method is commonly overridden in sub-classes.
		return getParseTarget().findDuplicate(r);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseTarget#parse(uk.ac.ed.epcc.safe.accounting.PropertyMap, java.lang.String)
	 */
	public final boolean parse(DerivedPropertyMap map, String current_line) throws AccountingParseException{
		return getParseTarget().parse(map, current_line);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseTarget#splitRecords(java.lang.String)
	 */
	public final Iterator<String> splitRecords(String update)
			throws AccountingParseException {
		return getParseTarget().splitRecords(update);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseTarget#deleteRecord(T)
	 */
	public final void deleteRecord(T old_record) throws Exception, DataFault {
		getParseTarget().deleteRecord(old_record);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseTarget#commitRecord(uk.ac.ed.epcc.safe.accounting.PropertyContainer, T)
	 */
	public final boolean commitRecord(PropertyContainer map, T record)
			throws DataFault {
		return getParseTarget().commitRecord(map, record);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseTarget#updateRecord(uk.ac.ed.epcc.safe.accounting.PropertyContainer, T)
	 */
	public final boolean updateRecord(DerivedPropertyMap map, T record)
			throws Exception {
		return getParseTarget().updateRecord(map, record);
	}
	public final boolean allowReplace(DerivedPropertyMap map, T record){
		return getParseTarget().allowReplace(map, record);
	}
	public final boolean isComplete(T record){
		return getParseTarget().isComplete(record);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseTarget#prepareRecord(uk.ac.ed.epcc.safe.accounting.DerivedPropertyMap)
	 */
	public final T prepareRecord(DerivedPropertyMap map) throws DataFault, InvalidPropertyException, AccountingParseException {
		return getParseTarget().prepareRecord(map);
	}
	public final StringBuilder endParse() {
		StringBuilder result=getParseTarget().endParse();
		res.setAllowNull(false);
		return result;
	}

	public final void startParse(PropertyMap defaults)
			throws Exception {
		res.setAllowNull(true);
		getParseTarget().startParse(defaults);
	}
	
	private PlugInOwner plugin_owner=null;
	protected abstract PlugInOwner makePlugInOwner(AppContext c,PropertyFinder prev, String tag);

	protected final PlugInOwner getPlugInOwner(){
		if( plugin_owner == null ){
			initAccessorMap(getContext(), getConfigTag());
		}
		return plugin_owner;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.PlugInOwner#getParser()
	 */
	public final PropertyContainerParser getParser(){
		return getPlugInOwner().getParser();
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.PlugInOwner#getPolicies()
	 */
	public final Set<PropertyContainerPolicy> getPolicies(){
		return getPlugInOwner().getPolicies();
	}


	

	public static class RescanTableTransition<P extends ParseUsageRecordFactory> extends AbstractFormTransition<P> implements ExtraFormTransition<P>{

	private static final String PERIOD = "Period";

	public static class RescanAction extends FormAction{
		private final ParseUsageRecordFactory target;
		public RescanAction(ParseUsageRecordFactory target){
			this.target=target;
		}
		@Override
		public FormResult action(Form f) throws ActionException {
			Period p = (Period) f.get(PERIOD);
			try {
				AndRecordSelector sel = new AndRecordSelector();
				sel.add(new PeriodOverlapRecordSelector(p, StandardProperties.ENDED_PROP));
				sel.add(new NullSelector<String>(StandardProperties.TEXT_PROP, false));
				int result[] = target.rescan(sel);
				return new MessageResult("data_loaded","Stored text",Integer.toString(result[0]),Integer.toString(result[1]),Integer.toBinaryString(result[2]));
			} catch (Exception e) {
				target.getContext().error(e,"Error rescaning");
				return new MessageResult("internal_error");
			}
		}
		
	}
	public void buildForm(Form f, P target, AppContext conn)
			throws TransitionException {
		f.addInput(PERIOD, PERIOD, new SimplePeriodInput());
		f.addAction("Regenerate", new RescanAction(target));
	}
	@Override
	public <X extends ContentBuilder> X getExtraHtml(X cb,
			SessionService<?> op, P target) {
		cb.addText("This operation will delete all selected records"+
			" and replace them with a new record parsed from the stored text from the original upload");
		return cb;
	}
	}
	public class ParseTableRegistry extends DataObjectTableRegistry{
		@SuppressWarnings("unchecked")
		public ParseTableRegistry() {
			super();
			if( getConfigTag().equals(getTag())){
				// Don't allow transitions where the config is taken from
				// a different table
				PlugInOwner owner = getPlugInOwner();
				if( owner instanceof TransitionSource){
					addTransitionSource((TransitionSource<ParseUsageRecordFactory<T>>)owner);
				}
				if( hasProperty(StandardProperties.TEXT_PROP) && hasProperty(StandardProperties.ENDED_PROP)){
					addTableTransition(new TransitionKey<ParseUsageRecordFactory>(ParseUsageRecordFactory.class, "Rescan", "Rescan all records stored as text"), new RescanTableTransition());
				}
			}
		}

		@Override
		public void getTableTransitionSummary(ContentBuilder hb, SessionService operator) {
			super.getTableTransitionSummary(hb, operator);
			if( plugin_owner instanceof SummaryProvider){
				((SummaryProvider) plugin_owner).getTableTransitionSummary(hb, operator);
			}
			
			
		}
		
	}
	@Override
	protected ParseTableRegistry makeTableRegistry(){
    	return new ParseTableRegistry();
	}


	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.tranistions.TableStructureTransitionTarget#resetStructure()
	 */
	public void resetStructure() {
		initAccessorMap(getContext(), getConfigTag());
		super.resetStructure();
	}
	/** Re-scan a set of records with a Text field. 
	 * This will only work if any meta-data properties consumed by parsers or policies in the startParse methos have been persisted within the
	 * database. This regenerates explicitly parsed values and merges them with the set of properties from the original record so any 
	 * values stored in the database that came from derived properties won't be overwritten even if the values used in the derivation have been changed. 
	 * @see ReParser
	 * @param sel 
	 * 
	 * @throws Exception 
	 */
	public int[] rescan(RecordSelector sel) throws Exception{
		int count=0;
		int good=0;
		int fail=0;
		int updates=0;
		if( hasProperty(StandardProperties.TEXT_PROP)){
			
			for(T rec : new FilterSet(getFilter(sel))){
				count++;
				try{
					// make all previous props available to start parse
					// as we may need some initial properties to perfrom setup
					DerivedPropertyMap map = new DerivedPropertyMap(getContext());
					map.setAll(rec);
					startParse(map);
					String text = rec.getProperty(StandardProperties.TEXT_PROP,null);
					if( text != null && text.trim().length() > 0){
						
						if( parse(map, text)){
							if(updateRecord(map, rec)){
								updates++;
							}
							good++;
						}else{
							fail++;
						}
					}
				}catch(AccountingParseException e){
					fail++;
					getContext().error(e,"Error in re-parse");
				}
			}
		}
		return new int[] {good,fail,updates};
	}
//	public String getUniqueID(T r) throws Exception {
//		
//		return getParseTarget().getUniqueID(r);
//	}
	
	@Override
	public void release() {
		if( mapi != null){
			mapi.release();
			mapi=null;
		}
		property_finder=null;
		super.release();
	}


}