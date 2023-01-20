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
package uk.ac.ed.epcc.safe.accounting.allocations.charged;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.IllegalReductionException;
import uk.ac.ed.epcc.safe.accounting.NumberSumReductionTarget;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.UsageRecordListener;
import uk.ac.ed.epcc.safe.accounting.allocations.AllocationFactory;
import uk.ac.ed.epcc.safe.accounting.allocations.AllocationKey;
import uk.ac.ed.epcc.safe.accounting.allocations.SequenceAllocationFactory;
import uk.ac.ed.epcc.safe.accounting.db.AccessorContributer;
import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.PropertyUpdater;
import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.NumberOp;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.result.ChainedTransitionResult;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractDirectTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableContentProvider;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** A ChargedAllocationFactory is 
 * a normal {@link AllocationFactory} but also implements {@link UsageRecordListener} to accumulate usage effectively acting as
 * an aggregation usage record with user defined time bounds. These aggregated properties can be paired with a manager edited value to
 * represent an allocation constraint. However Additional unconstrained properties can also be accumulated.
 * <p>
 * The property <b>parent.<em>table-name</em></b> defines the name of the {@link UsageProducer} where aggregated values come from.
 * </p>
 * <p>
 * The index properties and completion time are used to tie {@link ExpressionTargetContainer}s from the parent table to {@link ChargedAllocationRecord}s
 * </p>
 * <p>
 * Fields that match properties in the parent producer generate accumulation properties (with the same property tag) other fields are normal
 * allocations.
 * </p>
 * <p>Usage is accumulated in <em>ALL</em> allocations that match the target record. Each {@link ChargedAllocationRecord} therefore represents a
 * separate constraint rather than a set of allocations to be accumulated. The allocation records have to be constrained to be 
 * non-overlapping in order to represent a 
 * single sequence of allocations. Usage without any enclosing allocation is ignored</p>
 * <p> Enforcement of the constraint is a separate issue.
 * </p>
 * <p>
 * The accumulated properties are intended to produce quicker results that querying the {@link UsageProducer} directly for every report,
 * though this is also an option. 
 *  </p>
 * @author spb
 *
 * @param <T>
 */


public class ChargedAllocationFactory<T extends ChargedAllocationFactory.ChargedAllocationRecord,R> extends SequenceAllocationFactory<T,R> implements
		ChargedAllocationManager<AllocationKey<T>, T> , AccessorContributer<T>,
		TableContentProvider,TableTransitionContributor{
	

	private final UsageProducer<Use> parent;
	private final String parent_producer;
	  
	public ChargedAllocationFactory(AppContext c, String table) {
		this(c,table,null);
	}
	@SuppressWarnings("unchecked")
	public ChargedAllocationFactory(AppContext c, String table,UsageProducer<Use> parent_factory) {
		super(c, table);
		Logger log = c.getService(LoggerService.class).getLogger(getClass());
		log.debug("table is " + table);
		parent_producer = c.getInitParameter("parent." + table);
		log.debug("Parent tag is " + parent_producer);
		if( parent_factory == null ){
			AccountingService acct_service = c.getService(AccountingService.class);

			if( parent_producer != null){
				parent = acct_service.getUsageProducer(parent_producer);
			}else{
				parent=acct_service.getUsageProducer();
			}
		}else{
			parent=parent_factory;
		}
		assert(parent != null);
	}
	
	

	@Override
	public void customAccessors(AccessorMap<T> map, MultiFinder finder,PropExpressionMap derived) {
		if(parent != null){
			finder.addFinder(parent.getFinder());
			// add derived properties from parent 
			// that resolve
			
				PropExpressionMap props = parent.getDerivedProperties();
				
				map.addDerived(getContext(), props);
				map.clearUnresolvableDefinitions();
		}
	}



	private Set<PropertyTag<? extends Number>> accumulation_props;
	
	public final Set<PropertyTag<? extends Number>> getAccumulations(){
		if( accumulation_props == null){
			accumulation_props=Collections.unmodifiableSet(makeAccumulations());
		}
		return accumulation_props;
	}
	/** make the set of accumulated properties
	 * 
	 * @return
	 */
	protected Set<PropertyTag<? extends Number>> makeAccumulations(){
		LinkedHashSet<PropertyTag<? extends Number>> result = new LinkedHashSet<>();
		if( parent == null ){
			return result;
		}
		AccessorMap<T> map = getAccessorMap();
		for(PropertyTag<? extends Number> t : map.getProperties(Number.class)){
			if(parent.hasProperty(t) && map.getField(t)!=null){
				result.add(t);
			}
		}
		return result;
 	}
	private Map<PropertyTag<? extends Number>,PropertyTag<? extends Number>> constraints;
	/** This maps accumulation properties to an allocation property that is an allocation limit
	 * on the accumulated property. The allocation should be greater than the accumulation and this is taken into
	 * account when performing splits
	 * @return Map
	 */
	
	public final Map<PropertyTag<? extends Number>,PropertyTag<? extends Number>> getConstraints(){
		if( constraints == null){
			constraints=makeConstraints();
		}
		return constraints;
	}
	
	protected Map<PropertyTag<? extends Number>,PropertyTag<? extends Number>> makeConstraints(){
		// default to an empty map the subclasses will have to provide the constraints.
		return new HashMap<>();
	}
	@Override
	protected Set<PropertyTag> getSummaryProperties() {
		
		Set<PropertyTag> result = super.getSummaryProperties();
		result.addAll(getAccumulations());
		return result;
	}
	@Override
	protected Set<PropertyTag> getListProperties() {
		Set<PropertyTag> listProperties = super.getListProperties();
		listProperties.addAll(getAccumulations());
		return listProperties;
	}
	@Override
	protected Set<PropertyTag<? extends Number>> makeSplitProperties() {
		Set<PropertyTag<? extends Number>> result = super.makeSplitProperties();
		result.addAll(getAccumulations());
		return result;
	}
@Override
	protected Set<PropertyTag<? extends Number>> makeAllocationProperties() {
		// only want properties not in the parent table
		Set<PropertyTag<? extends Number>> result = super.makeAllocationProperties();
		PropertyFinder parent_finder = parent.getFinder();
		for(Iterator it = result.iterator(); it.hasNext();){
			PropertyTag tag = (PropertyTag) it.next();
			if( parent_finder.hasProperty(tag)){
				it.remove();
			}
		}
		return result;
	}
@Override
	protected T makeBDO(Record res) throws DataFault {
		return (T) new ChargedAllocationRecord(this,res);
	}


	
	public static class ChargedAllocationRecord extends AllocationFactory.AllocationRecord{

		protected ChargedAllocationRecord(ChargedAllocationFactory fac, Record r) {
			super(fac, r);
		}
		public final ChargedAllocationFactory getChargedAllocationFactory(){
			return (ChargedAllocationFactory) getUsageRecordFactory();
		}
		
		@SuppressWarnings("unchecked")
		public final void regenerate() throws Exception{
			// Final to force all code through the factory
			// version otherwise the t versions might be extended
			// differently
			getChargedAllocationFactory().regenerate(this);
		}
	}
	/** transition to regenerate the accumulated values
	 * 
	 * @author spb
	 *
	 */
	public class RegenerateTransition extends AbstractDirectTransition<T>{

		public FormResult doTransition(T target, AppContext c)
				throws TransitionException {
			try {
				regenerate(target);
			} catch (Exception e) {
				getLogger().error("Error from regenerate",e);
				throw new TransitionException("Internal error");
			}
			return new ChainedTransitionResult<>(ChargedAllocationFactory.this, target, null);
		}
		
	}
	
	
	@Override
	protected boolean editEnds() {
		return false;
	}
	@Override
	protected void addTransitions(LinkedHashMap<AllocationKey<T>, Transition<T>> res){
		super.addTransitions(res);
		res.put(new AllocationKey<T>(ChargedAllocationRecord.class, "Regenerate","recalculate time used from accounting data"),new RegenerateTransition());
	}
	/** recalculate charged values from scratch for a record
	 * 
	 * @param record
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void regenerate(T record) throws Exception{
		Set<ReductionTarget> list = new LinkedHashSet<>();
		Set<PropertyTag<? extends Number>> acc = getAccumulations();
		for( PropertyTag t : acc){
			list.add(new NumberSumReductionTarget( t));
		}
		AndRecordSelector sel = new AndRecordSelector();
		sel.add(new SelectClause<>(StandardProperties.ENDED_PROP,MatchCondition.GE, record.getStart()));
		sel.add(new SelectClause<>(StandardProperties.ENDED_PROP,MatchCondition.LT, record.getEnd()));
		for(ReferenceTag t : getIndexProperties()){
			sel.add(new SelectClause<IndexedReference>(t, record));
		}
		// only expect one value but easiest to loop
		for( Map<ReductionTarget,Object> data : parent.getIndexedReductionMap(list, sel).values()){
			for( ReductionTarget r : data.keySet() ){
				record.setProperty((PropertyTag)r.getExpression(), data.get(r));
			}
		}
		record.commit();
	}
	/** Do a direct reduction on the parent {@link UsageProducer}
	 * for records that end overlapping with the allocation
	 * 
	 * @param record
	 * @param expr
	 * @return
	 * @throws IllegalReductionException
	 * @throws Exception
	 */
	public Number queryOverlap(T record,PropExpression<? extends Number> expr) throws IllegalReductionException, Exception{
		AndRecordSelector sel = new AndRecordSelector();
		for(ReferenceTag t : getIndexProperties()){
			sel.add(new SelectClause<IndexedReference>(t, record));
		}
		sel.add(new PeriodOverlapRecordSelector(record, StandardProperties.ENDED_PROP));
		return parent.getReduction(new NumberSumReductionTarget(expr), sel);
	}
	/** regenerate all records
	 * 
	 * @throws Exception
	 */
	public void regenerateAll() throws Exception{
		for(T t: all()){
			//regenerate(t);
			// notify calls regenerate and invokes listeners.
			notifyModified(t, "regenerateAll");
		}
	}
	/** get all allocations that match a {@link PropertyContainer}
	 * 
	 * @param c
	 * @return Iterator
	 * @throws Exception 
	 * @throws InvalidPropertyException
	 */
	public CloseableIterator<T> getMatches(PropertyContainer c) throws Exception{
    	AndRecordSelector sel;
    	sel = getMatchFilter(c);
    	return getExpressionTargetFactory().getIterator(sel);
    }
	@SuppressWarnings("unchecked")
	private AndRecordSelector getMatchFilter(PropertyContainer c) throws InvalidExpressionException {
		AndRecordSelector sel;
		sel = new AndRecordSelector();
    	sel.add(new SelectClause<>(StandardProperties.STARTED_PROP,MatchCondition.LE,c.getProperty(StandardProperties.ENDED_PROP)));
    	sel.add(new SelectClause<>(StandardProperties.ENDED_PROP,MatchCondition.GT,c.getProperty(StandardProperties.ENDED_PROP)));
    	for(ReferenceTag t : getIndexProperties()){
    		sel.add(new SelectClause<IndexedReference>(t,c));
    	}
		return sel;
	}
	
    @SuppressWarnings("unchecked")
	private void aggregate(PropertyContainer rec, boolean add)
	throws Exception {
    	PropertyUpdater<T> updater = new PropertyUpdater<>(this);
    	// Perform in-database update so updates are atomic (avoid read/modify/write cycle)
    	AndRecordSelector fil = getMatchFilter(rec);
    	for(PropertyTag<? extends Number> t : getAccumulations()){
    		Number val = (Number) rec.getProperty(t);
    		PropExpression<? extends Number> val_expr = new ConstPropExpression<>(Number.class,val);
    		Operator op = add ? Operator.ADD : Operator.SUB;
    		PropExpression expr = new BinaryPropExpression(t, op, val_expr);
    		updater.update(t,expr,fil);
    	}
    	for(Iterator<T> it = getMatches(rec); it.hasNext();){
    		T agg = it.next();
    		notifyAggregate(agg, rec, add);
    	}
    }


    
	public void startListenerParse() {
		
		
	}

	public String endListenerParse() {
		
		return null;
	}

	public final void postCreate(PropertyContainer props, ExpressionTargetContainer rec)
			throws Exception {
		aggregate(rec, true);
		
	}

	public void preDelete(ExpressionTargetContainer rec) throws Exception {
		aggregate(rec, false);
	}
	public class RegenerateAllTransition extends AbstractDirectTransition<ChargedAllocationFactory<T,R>>{

		public FormResult doTransition(ChargedAllocationFactory<T,R> target,
				AppContext c) throws TransitionException {
			try {
				target.regenerateAll();
			} catch (Exception e) {
				getLogger().error("Error in regenerateAll",e);
				throw new TransitionException("internal error");
			}
			return new ViewTableResult(target);
		}
		
	}
	
	@Override
	protected Set<String> getSupress() {
		// We don't want to edit the accumulated fields 
		AccessorMap map = getAccessorMap();
		Set<String> supress_fields = new HashSet<>();
		for(PropertyTag local : getAccumulations()){
			String field = map.getField(local);
			if( field != null ){
				supress_fields.add(field);
			}
		}
		return supress_fields;
	}
	
	@Override
	public void notifySplit(T first, T second) {
		
		try{
			regenerate(first);
			regenerate(second);
			Map<PropertyTag<? extends Number>,PropertyTag<? extends Number>> cons = getConstraints();
			for(PropertyTag<? extends Number> acc : cons.keySet()){
				PropertyTag<? extends Number> limit = cons.get(acc);
				Number first_used = first.getProperty(acc);
				Number second_used = second.getProperty(acc);
				
				Number first_limit = first.getProperty(limit);
				Number second_limit = second.getProperty(limit);
				
				if( first_used.doubleValue() > first_limit.doubleValue()){
					// Move allocation to first period
					first.setProperty((PropertyTag<Number>)limit, first_used);
					second.setProperty((PropertyTag<Number>)limit, NumberOp.sub(second_limit,NumberOp.sub(first_used,first_limit)));
				}

				if( second_used.doubleValue() > second_limit.doubleValue()){
					// Move allocation to first period
					second.setProperty((PropertyTag<Number>)limit, second_used);
					first.setProperty((PropertyTag<Number>)limit, NumberOp.sub(first_limit,NumberOp.sub(second_used,second_limit)));
				}
				
			}
		}catch(Exception e){
			getLogger().error("Error regenerating use",e);
		}
		super.notifySplit(first, second);
	}
	@Override
	public void notifyCreated(T rec) {
		try {
			regenerate(rec);
		} catch (Exception e) {
			getLogger().error("Error regenerating after creation",e);
		}
		super.notifyCreated(rec);
	}
	@Override
	public void setStart(T period, Date d) throws Exception {
		super.setStart(period, d);
		regenerate(period);
	}
	@Override
	public void setEnd(T period, Date d) throws Exception {
		super.setEnd(period, d);
		regenerate(period);
	}
	@Override
	public void notifyModified(T rec, String details) {
		try {
			regenerate(rec);
		} catch (Exception e) {
			getLogger().error("Error regenerating after creation",e);
		}
		super.notifyModified(rec, details);
	}
	private void addTable(ContentBuilder hb,String title,Set<? extends PropertyTag> set){
		hb.addHeading(4,title);
		Table t = new Table();
		for(PropertyTag<?> tag : set){
			t.put("Property Name", tag, tag.getFullName());
			t.put("Desciption", tag, tag.getDescription());
		}
		hb.addTable(getContext(), t);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactory#addSummaryContent(uk.ac.ed.epcc.webapp.content.ContentBuilder)
	 */
	@Override
	public void addSummaryContent(ContentBuilder hb) {
		if( parent == null) {
			hb.addHeading(3, "No Parent Producer");
		}else {
			hb.addHeading(3,"Parent Producer: "+parent.getTag());
		}
		addTable(hb,"Index Properties",getIndexProperties());
		addTable(hb, "Allocation properties", getAllocationProperties());
		addTable(hb,"Accumulation properties",getAccumulations());
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactory#getTableTransitions()
	 */
	@Override
	public Map<TableTransitionKey, Transition> getTableTransitions() {
		Map<TableTransitionKey, Transition> tableTransitions = new LinkedHashMap<>();
		
		
		tableTransitions.put(new AdminOperationKey("Regenerate"), new RegenerateAllTransition());
		
		return tableTransitions;
	}
	
}