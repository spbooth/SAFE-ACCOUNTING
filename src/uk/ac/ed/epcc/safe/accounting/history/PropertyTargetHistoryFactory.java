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
package uk.ac.ed.epcc.safe.accounting.history;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.PropertyImplementationProvider;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.CompatibleSelectVisitor;
import uk.ac.ed.epcc.safe.accounting.db.FilterSelectVisitor;
import uk.ac.ed.epcc.safe.accounting.db.MapFinder;
import uk.ac.ed.epcc.safe.accounting.db.PropertyMaker;
import uk.ac.ed.epcc.safe.accounting.db.ReductionHandler;
import uk.ac.ed.epcc.safe.accounting.db.RepositoryAccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.transitions.TableRegistry;
import uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DoubleDeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ConfigPropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.ConstExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.IndexedFieldValue;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.iterator.SkipIterator;
import uk.ac.ed.epcc.webapp.model.history.HistoryFactory;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;

/** This class extends {@link HistoryFactory} for peer classes that are {@link ExpressionTarget}s.
 * This class also acts as a UsageProducer allowing the history data to be used in reports. 
 * 
 * @author spb
 *
 * @param <T>
 * @param <F>
 * @param <H> 
 */


public class PropertyTargetHistoryFactory<T extends DataObject , F extends DataObjectFactory<T>, H extends PropertyTargetHistoryFactory.HistoryUse<T>> 
extends HistoryFactory<T,H> implements ExpressionTargetFactory<H>,UsageProducer<H>,ExpressionFilterTarget<H>,PropertyImplementationProvider{

	/** HistoryRecord extended to be an ExpressionTarget
	 * 
	 * @author spb
	 *
	 * @param <T>
	 */
	public static  class HistoryUse<T extends DataObject> extends HistoryFactory.HistoryRecord<T> implements ExpressionTargetContainer,ExpressionTarget,TimePeriod{
		private final ExpressionTargetContainer proxy;
		@SuppressWarnings("unchecked")
		public HistoryUse(PropertyTargetHistoryFactory<T,?,?> fac,Record res) {
			super(fac, res);
			AccessorMap map = fac.getAccessorMap();
			proxy = map.getProxy(this);
		}
		@SuppressWarnings("unchecked")
		PropertyTargetHistoryFactory<T,?,HistoryUse<T>> getFac(){
			return (PropertyTargetHistoryFactory<T, ?, HistoryUse<T>>) history_factory;
		}

		public final <X> X getProperty(PropertyTag<X> tag) throws InvalidExpressionException {
			return proxy.getProperty(tag);
		}
		public final <X> X getProperty(PropertyTag<X> tag, X def) {
			return proxy.getProperty(tag, def);
		}
		public final <X> X evaluateExpression(PropExpression<X> expr)
		throws InvalidExpressionException {
			return proxy.evaluateExpression(expr);
		}
		public final <X> X evaluateExpression(PropExpression<X> expr, X def){
					return proxy.evaluateExpression(expr,def);
		}
		public final <X> void setProperty(PropertyTag<? super X> tag, X value) throws InvalidPropertyException {
			proxy.setProperty(tag, value);
		}
		public final <X> void setOptionalProperty(PropertyTag<? super X> tag, X value) {
			proxy.setOptionalProperty(tag, value);
		}
		public final <X> void setProperty(PropertyTag<X> tag, PropertyContainer map) throws InvalidExpressionException{
			setProperty(tag,map.getProperty(tag));
		}
		
		public final boolean supports(PropertyTag<?> tag){
			return proxy.supports(tag);
		}
		public final boolean writable(PropertyTag<?> tag){
			return proxy.writable(tag);
		}
		public ExpressionTargetFactory getExpressionTargetFactory() {
			return getFac();
		}

		/** Unique key for populating a Table 
		 * Note the Table may have entries from more than one table
		 *  @return Object unique to this table and record
		 */
		public final Object getKey() {
			// implicit dependency with AccountingManager.AccountingRecordUpdator
			return getFactoryTag() +":"+ getID();
		}
		public Set<PropertyTag> getDefinedProperties() {
			return proxy.getDefinedProperties();
		}
		public void setAll(PropertyContainer source) {
			proxy.setAll(source);
			
		}
		public Parser getParser() {
			return proxy.getParser();
		}
		public Date getEnd() {
			return getProperty(StandardProperties.ENDED_PROP,null);
		}		

		public Date getStart()  {
			return getProperty(StandardProperties.STARTED_PROP,null);
		}
		
		
		@Override
		public void release(){
			super.release();
			proxy.release();
		}

	}
	
	private RepositoryAccessorMap<H> mapi=null;
	private PropertyFinder property_finder=null;
	public static final PropertyRegistry history= new PropertyRegistry("history","History table properties");
	public static final PropertyTag<Date> HISTORY_START = new PropertyTag<Date>(history,START_TIME_FIELD,Date.class,"Start of history period");
	public static final PropertyTag<Date> HISTORY_END = new PropertyTag<Date>(history,END_TIME_FIELD,Date.class,"End of history period");
	private PropertyRegistry peer;
	private ReferenceTag<T, F> PEER;
	
	public PropertyTargetHistoryFactory(F fac, String table) {
		super(fac, table);
		property_finder=null;
		mapi=null;
	}
	@SuppressWarnings("unchecked")
	private void initAccessorMap(AppContext conn, String tag){
		MultiFinder finder = new MultiFinder();
		PropExpressionMap derived = new PropExpressionMap();
		mapi = new RepositoryAccessorMap<H>(this,res);
		finder.addFinder(StandardProperties.base);
		
		
		mapi.put(StandardProperties.COUNT_PROP, new ConstExpression<Number,H>(Number.class,new Integer(1)));
		ReferencePropertyRegistry ref_registry = ReferencePropertyRegistry.getInstance(conn);
		mapi.makeReferences(ref_registry);
		finder.addFinder(ref_registry);
		F fac = (F) getPeerFactory();
		peer = new PropertyRegistry(tag+"history", "history properties for "+tag);
		PEER = new ReferenceTag<T, F>(peer, "peer", (Class<F>) fac.getClass(), fac.getTag());
		IndexedFieldValue referenceExpression = res.getReferenceExpression(getTarget(),getPeerName(),fac);
		mapi.put(PEER,  referenceExpression);
		// add in config registries
		String list = conn.getInitParameter("registry_list."+tag, "expression");
		for( String name : list.split("\\s*,\\s*")){
			finder.addFinder(new ConfigPropertyRegistry(conn, name));
		}
	
		if( fac instanceof PropertyTargetFactory){
			PropertyTargetFactory ptf = (PropertyTargetFactory) fac;
		// import properties from peer.
			PropertyFinder peer_props = ptf.getFinder();
			finder.addFinder(peer_props);
			if( fac instanceof ExpressionTargetFactory){
				ExpressionTargetFactory etf = (ExpressionTargetFactory) fac;
				AccessorMap peer_map = etf.getAccessorMap();
				PropExpressionMap peer_derived = peer_map.getDerivedProperties();
				for(PropertyTag t : peer_props.getProperties()){
					if( etf.hasProperty(t)){
						try{
							if( peer_map.isDerived(t)){
								
								// if its an expression add the same expression here
								// we want to use the history values in preference to the current.
								derived.put(t, peer_derived.get(t));
							}else{
								// Fall back to getting value from peer 
								// We have a problem if the implementation is a "magic" accessor that calculates quantities based on other properties.
								// This will calculate the properties based on the current peer state not the historical state so we don't want to forward to
								// on the other hand reference tags need special handling
								if( t instanceof ReferenceExpression){
									derived.put(t, new DoubleDeRefExpression(PEER, (ReferenceExpression) t));
								}else{
									if( !  peer_map.isAccessor(t)){
										derived.put(t, new DeRefExpression(PEER, t));
									}
								}
							}
						}catch(Exception e){
							getLogger().error("Error adding peer property",e);
						}
					}	
				}
			}else{
				for(PropertyTag t : peer_props.getProperties()){
					try{
						if( ptf.hasProperty(t)){
							// Fall back to getting value from peer
							derived.put(t, new DeRefExpression(PEER, t));
						}
					}catch(Exception e){
						getLogger().error("Error adding peer property",e);
					}
				}
			}
		}
		customAccessors(mapi, finder, derived);
		// add config overrides
		derived.addFromProperties(finder, conn, tag);
		finder.addFinder(history);
		if( useHistoryAsTimeBounds()){
			try{
				derived.put(StandardProperties.STARTED_PROP, HISTORY_START);
				derived.put(StandardProperties.ENDED_PROP, HISTORY_END);
			}catch(Exception e){
				getLogger().error("Unexpected exception",e);
			}
		}
		
		PropertyRegistry table_reg = new PropertyRegistry(tag, "Fields from table "+tag);
		mapi.populate( finder, table_reg, false);
		finder.addFinder(table_reg);
		
		mapi.addDerived(conn, derived);
		property_finder=finder;
	}

	/** Extension point to allow custom accessors and registries to be added.
	 * 
	 * @param mapi2
	 * @param finder
	 * @param derived
	 */
	protected void customAccessors(AccessorMap<H> mapi2, MultiFinder finder,
			PropExpressionMap derived) {
		
	}
	public final PropertyFinder getFinder(){
		if(property_finder == null ){
			initAccessorMap(getContext(), getConfigTag());
		}
		return property_finder;
	}
	/** Should the history time bounds be used as the {@link ExpressionTargetContainer} time bounds
	 * 
	 * This defaults to true but should be overridden if the peer is itself a UsageRecord
	 * and the history table is being used to view reports as seen from the past.
	 * 
	 * @return
	 */
    protected boolean useHistoryAsTimeBounds(){
    	return true;
    }
	protected TableRegistry makeTableRegistry() {
		return new TableRegistry(res,getFinalTableSpecification(getContext(), getTag()),getProperties(),getAccessorMap());
	}
	

	

	public final RepositoryAccessorMap<H> getAccessorMap(){
		if( mapi == null ){
			initAccessorMap(getContext(), getConfigTag());
		}
		return mapi;
	}

	@Override
	protected Map<String, Object> getSelectors() {
		
		RepositoryAccessorMap<H> map = getAccessorMap();
		return map.getSelectors();
		
	}
	
	
	

	
	/**
	 * Get the set of <em>Defined</em> properties.
	 * 
	 * @return Set<PropertyTag>
	 */
	public final Set<PropertyTag> getProperties() {
		return getAccessorMap().getProperties();
	}
	/**
	 * Is this a defined property
	 * 
	 * @param p
	 * @return boolean
	 */
	public final <X> boolean hasProperty(PropertyTag<X> p) {
		return getAccessorMap().hasProperty(p);
	}

	

	public final <R> BaseFilter<H> getFilter(PropExpression<R> expr,
			MatchCondition match, R value) throws CannotFilterException{
		return getAccessorMap().getFilter( expr, match, value);
	}
	
	public <R> BaseFilter<H> getRelationFilter(PropExpression<R> left,
			MatchCondition match, PropExpression<R> right)
			throws CannotFilterException {
		return getAccessorMap().getRelationFilter(left, match, right);
	}
	public <R> BaseFilter<H> getNullFilter(PropExpression<R> expr,
			boolean is_null) throws CannotFilterException {
		return getAccessorMap().getNullFilter(expr, is_null);
	}

		
	public <I> SQLFilter<H> getOrderFilter(boolean descending, PropExpression<I> expr)
			throws CannotFilterException {
		return getAccessorMap().getOrderFilter(descending, expr);
	}
	

	
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget#getRelationshipFilter(java.lang.String)
	 */
	@Override
	public BaseFilter<H> getRelationshipFilter(String relationship) throws CannotFilterException {
		return getAccessorMap().getRelationshipFilter(relationship);
	}
	/** Get a filter from a PropertyMap selector
	 * 
	 * @param selector
	 * @return
	 * @throws CannotFilterException 
	 */
	
	protected BaseFilter<H> getFilter(RecordSelector selector) throws CannotFilterException {
		if( selector == null ){
			return null;
		}
		try {
			return selector.visit(new FilterSelectVisitor<H>(this));
		}catch(CannotFilterException e){
			throw e;
		} catch (Exception e) {
			throw new CannotFilterException(e);
		}
	}

	public Iterator<H> getIterator(RecordSelector sel, int skip, int count)
			throws Exception {
		BaseFilter<H> filter = getFilter(sel);
		try{
			return this.new FilterIterator(FilterConverter.convert(filter),skip,count);
		}catch(NoSQLFilterException e){
			return new SkipIterator<H>(new FilterIterator(filter), skip, count);
		}
	}

	public Iterator<H> getIterator(RecordSelector sel) throws Exception {
		return new FilterIterator(getFilter(sel));
	}


	public long getRecordCount(RecordSelector selector) throws Exception {
		 if( ! compatible(selector)){
	        	return 0L;
		 }
		 return getCount(getFilter(selector));
	}
	private ReductionHandler<H,PropertyTargetHistoryFactory<T,F,H>> getReductionHandler(){
		return new ReductionHandler<H,PropertyTargetHistoryFactory<T,F,H>>(this);
	}

	public <I> Map<I, Number> getReductionMap(PropExpression<I> index,
			ReductionTarget<Number> property,  RecordSelector selector)
			throws Exception 
	{
		return getReductionHandler().getReductionMap(index, property, selector);
		
	}

	
	public  Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap( Set<ReductionTarget> sum, RecordSelector selector) throws Exception{
		return getReductionHandler().getIndexedReductionMap(sum, selector);
	}
	
	
	public  <R>  R getReduction(ReductionTarget<R> type, RecordSelector selector) throws Exception {
		return getReductionHandler().getReduction(type, selector);
	}
	
	
	


	public <PT> Set<PT> getValues(PropertyTag<PT> tag, RecordSelector selector) throws DataException, InvalidExpressionException, CannotFilterException {
		if( ! hasProperty(tag)){
			return new HashSet<PT>();
		}
		BaseFilter<H> filter = getFilter(selector);	
		try{
			PropertyMaker<H,PT> finder = new PropertyMaker<H,PT>(getAccessorMap(),res,tag, true);			
			return finder.find(FilterConverter.convert(filter));
		}catch(CannotUseSQLException e){
			Set<PT> result = new HashSet<PT>();
			for(H o : new FilterSet(filter)){
				result.add(o.getProperty(tag));
			}
			return result;
		}	
	}
	
	public <I, P> Map<I, P> getPropMap(PropertyTag<I> index,
			PropertyTag<P> property,  RecordSelector selector)
			throws DataException, CannotFilterException, InvalidExpressionException 
	{
		Logger log = getContext().getService(LoggerService.class).getLogger(getClass());
		
		if( !(hasProperty(index) && hasProperty(property) && compatible(selector))){
			// no matching property
			return new HashMap<I,P>();
			
		}
		log.debug("matching property "+index.toString()+" or "+property.toString());
		BaseFilter<H> filter = getFilter(selector);
		try{
			SQLFilter<H> sql_fil = FilterConverter.convert(filter);
		
			MapFinder<H,I,P> finder = new MapFinder<H,I,P>(getAccessorMap(),res, index, property);
			
			return finder.find(sql_fil);
		}catch(CannotUseSQLException e){
			HashMap<I,P> result = new HashMap<I, P>();
			for(H o: new FilterSet(filter)){
				result.put(o.getProperty(index), o.getProperty(property));
			}
			return result;
		}
	}
	
	
	
	
	
	
	public boolean compatible(RecordSelector sel) {
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(this,false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			//log.debug("Selector "+sel.toString()+" not compatible with "+getTag(),e );
			return false;
		}
	}
	@Override
	public Class<? super H> getTarget() {
		return HistoryUse.class;
	}
	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		
		return new HistoryUse<T>(this, res);
	}
	public void resetStructure() {
		super.resetStructure();
		mapi=null;
		property_finder=null;
		
	}
	
	public <I> boolean compatible(PropExpression<I> expr) {
		return getAccessorMap().resolves(expr,false);
	}
	
	public BaseFilter<H> getPeriodFilter(Period period,
			PropExpression<Date> start, 
			PropExpression<Date> end, OverlapType type, long cutoff)
			throws CannotFilterException {
		return getAccessorMap().getPeriodFilter(period, start, end,type,cutoff);
	}
	public String getUniqueID(H r) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(getTag());
		sb.append("-");
		sb.append(r.getPeerID());
		sb.append("-");
		sb.append(r.getStartTimeAsDate().getTime()); // start is enough as sequence and end may change
		return sb.toString();
	}
	public boolean isMyRecord(ExpressionTargetContainer record) {
		if( getTarget().isAssignableFrom(record.getClass())){
			return isMine((DataObject) record);
		}
		return false;
	}
	
	@Override
	public void release() {
		if( mapi != null){
			mapi.release();
			mapi=null;
		}
		property_finder=null;
		super.release();
	}
	@Override
	public final String getImplemenationInfo(PropertyTag<?> tag) {
		return getAccessorMap().getImplemenationInfo(tag);
	}
}