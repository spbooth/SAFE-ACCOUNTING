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
package uk.ac.ed.epcc.safe.accounting.aggregation;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.DateReductionTarget;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.IndexReduction;
import uk.ac.ed.epcc.safe.accounting.NumberSumReductionTarget;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageManager;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.UsageRecordListener;
import uk.ac.ed.epcc.safe.accounting.db.AccessorContributer;
import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.expr.ResolvesWithoutChecker;
import uk.ac.ed.epcc.safe.accounting.policy.ListenerPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTuple;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.NumberOp;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.ExtendedXMLBuilder;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.SimplePeriodInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractDirectTransition;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.DatabaseService;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLAndFilter;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.DateFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.FieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableContentProvider;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.filter.FilterDelete;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
/** A UsageRecordFactory that generates aggregate records from a separate class.
 * The tables in the parent {@link UsageProducer} will require a {@link ListenerPolicy} to populate the aggregate records if data 
 * is ever loaded into them. Any "rolled" tables of old data do not need the policy but have to be in the parent UsageProducer for
 * so the historical data will be included if the aggregates are re-generated.
 * Property definitions follow those of the parent table. By default Numerical properties
 * are summed where other properties identify different aggregation streams 
 * 
 * The start and end time properties are rounded up/down to regular time boundaries so that 
 * all records within a aggregate have run over a similar time frame. The selected aggregation period will enclose the actual record but have start/end times aligned to the requested time quantum.  In practice this will tend to
 * make long running jobs less likely to be aggregated unless they are part of a related set of jobs. 
 * <p>
 * Configuration properties
 * <ul>
 * <li> <b>master.<i>table</i></b> name of the original un-aggregated {@link UsageProducer}.</li>
 * <li> <b>key.<i>table</i>.<i>prop-name</i></b> boolean property to force a numeric property to be a key prop
 * <li> <b><i>table</i>.aggregate_using_end</b> Force the aggregation periods to depend only on the completion time. In this case the aggregate
 * represents records that completed within the aggregate time frame
 * </ul>
 * 
 * 
 * @author spb
 *
 */
public abstract class AggregateUsageRecordFactory
		extends UsageRecordFactory<AggregateUsageRecordFactory.AggregateRecord> 
implements UsageRecordListener, 
AccessorContributer<AggregateUsageRecordFactory.AggregateRecord>,
TableContentProvider,
TableTransitionContributor,
ConfigParamProvider{
	public static final String MASTER_PREFIX = "master.";
	private static final String COMPLETED_TIMESTAMP = "CompletedTimestamp";
	private static final String STARTED_TIMESTAMP = "StartedTimestamp";
	private static final Feature USE_FAST_REGENERATE = new Feature("aggregate.use_fast_regenerate",true,"Use reductions to speed up regenerate");
	public static final AdminOperationKey REGENERATE = new AdminOperationKey("Regenerate","Repopulate all contents from parent table");
	public static final AdminOperationKey REGENERATE_RANGE = new AdminOperationKey("RegenerateRange","Repopulate contents for a specified time period");
	public static final PropertyRegistry aggregate = new PropertyRegistry("aggregate","Time bounds for aggregate records");
	public static final PropertyTag<Date> AGGREGATE_STARTED_PROP = new PropertyTag<>(aggregate,STARTED_TIMESTAMP,Date.class);
	public static final PropertyTag<Date> AGGREGATE_ENDED_PROP = new PropertyTag<>(aggregate,COMPLETED_TIMESTAMP,Date.class);


	public final static class AggregateRecord extends UsageRecordFactory.Use{

		protected AggregateRecord(AggregateUsageRecordFactory fac, Record r) {
			super(fac, r);
		}
		/**
		 * 
		 * @param rec
		 * @param start_point aggregate period start
		 * @param end_point aggregate period end
		 * @return true if matches
		 */
		@SuppressWarnings("unchecked")
		public boolean matches(PropertyContainer rec,Date start_point,Date end_point) {
			
			if( ! start_point.equals(getStart()) ){
				return false;
			}
			if( ! end_point.equals(getEnd()) ){
				return false;
			}
			ExpressionTargetContainer proxy = getProxy();
			ExpressionTargetFactory etf = getExpressionTargetFactory();
			for( PropertyTag t : ((AggregateUsageRecordFactory)getFactory()).getKeyProperties()){
				if( etf.hasProperty(t) ){
					Object a = proxy.getProperty(t,null);
					if( a != null ){
						Object property = rec.getProperty(t,null);
						if(! a.equals(property)){
							return false;
						}
					}else{
						if( rec.getProperty(t,null) != null ){
							return false;
						}
					}
				}	
			}
			return true;
		}
		/* (non-Javadoc)
		 * @see uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory.Use#getEnd()
		 */
		@Override
		public Date getEnd() {
			return getProxy().getProperty(AGGREGATE_ENDED_PROP,null);
		}
		/* (non-Javadoc)
		 * @see uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory.Use#getStart()
		 */
		@Override
		public Date getStart() {
			return getProxy().getProperty(AGGREGATE_STARTED_PROP,null);
		}
	}
	private UsageProducer<Use> parent;
	
	private  PropertyTag<Date> end_target_prop;
	private  PropertyTag<Date> start_target_prop;
	private  Set<PropertyTag> key_set;
	private  Set<PropertyTag<? extends Number>> sum_set;
    private  long raw_counter=0;
    private long fetch_counter=0;
	// cache values. In practice large numbers of sequential records will 
	// target the same aggregate record so we cache the last used values.
    private static final int CACHE_SIZE=16;
    private int replace=0;
	private AggregateRecord cache[] = new AggregateRecord[CACHE_SIZE];
    private ReferencePropertyRegistry ref_registry;
    private String parent_producer=null;
  
    @Override
    protected TableSpecification getDefaultTableSpecification(AppContext ctx, String homeTable){
    	TableSpecification spec = super.getDefaultTableSpecification(ctx, homeTable);
		spec.setField(STARTED_TIMESTAMP, new DateFieldType(true, null));
		spec.setField(COMPLETED_TIMESTAMP, new DateFieldType(true, null));
		UsageProducer prod = parent;
		if( prod instanceof UsageManager) {
			for(DataObjectFactory fac : ((UsageManager<?>)parent).getProducers(DataObjectFactory.class)) {
				prod=(UsageProducer) fac;
				break;
			}
		}
		if( prod != null  && prod instanceof DataObjectFactory) {
			TableSpecification parent_spec = ((DataObjectFactory)prod).getFinalTableSpecification(getContext(), parent_producer);
			if( parent_spec != null) {
				for( String name : parent_spec.getFieldNames()) {
					FieldType f = parent_spec.getField(name);
					if( f.geTarget() != Date.class && ! spec.hasField(name)) {
						spec.setOptionalField(name, f);
					}
				}
			}
		}
		try {
			// Note lots of entries will have duplicate values
			spec.new Index("end_key", false, COMPLETED_TIMESTAMP);
		} catch (InvalidArgument e1) {

		}
		return spec;
    }
	@SuppressWarnings("unchecked")
	protected AggregateUsageRecordFactory(UsageRecordFactory parent_factory) {	
		this.parent = parent_factory;
		if( parent_factory != null) {
			this.parent_producer=parent_factory.getTag();
		}
	}


@Override
	protected void postSetContext() {
		super.postSetContext();
		AppContext c = getContext();
		String tag = getTag();
		if( parent == null ){
			parent_producer = c.getInitParameter(MASTER_PREFIX + tag);
			if( parent_producer != null){
				parent = c.getService(AccountingService.class).getUsageProducer(parent_producer);
			}
		}
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyFactory#getConfigProperties()
	 */
	@Override
	public void addConfigParameters(Set<String> props) {
		props.add(MASTER_PREFIX+getConfigTag());
		props.add(getConfigTag()+".aggregate_using_end");
		// props to force/reset key/sum
		for( PropertyTag t : getKeyProperties()) {
			if( Number.class.isAssignableFrom(t.getTarget())) {
				props.add(getConfigName(t));
			}
		}
		for( PropertyTag t : getSumProperties()) {
			if( Number.class.isAssignableFrom(t.getTarget())) {
				props.add(getConfigName(t));
			}
		}
	}
	
	
	private String getConfigName(PropertyTag t) {
		return "key."+getConfigTag()+"."+t.getName();
	}
	@Override
	public void customAccessors(AccessorMap<AggregateUsageRecordFactory.AggregateRecord> map, MultiFinder finder,PropExpressionMap derived) {
		AppContext c = getContext();
		String tag = getTag();
		Logger log = c.getService(LoggerService.class).getLogger(getClass());
		log.debug("table is " + tag);
		

		finder.addFinder(StandardProperties.base); // need the date properties
		if( parent != null ){
			finder.addFinder(parent.getFinder());
		}
		assert(aggregate.hasProperty(AGGREGATE_STARTED_PROP));
		finder.addFinder(aggregate); // match these in preference we don't want date properties
		// from the parent binding to the date fields






		if( c.getBooleanParameter(tag+".aggregate_using_end", false)){
			// Use just end date to define aggregate limits
			start_target_prop = StandardProperties.ENDED_PROP;
		}else{
			start_target_prop = StandardProperties.STARTED_PROP;
		}

		end_target_prop = StandardProperties.ENDED_PROP;

		
		// add derived properties from parent but we don't want any that use the 
		// date values as the definitions may not be correct. 
		PropExpressionMap props=parent.getDerivedProperties();
		
		if( props != null) {
			Set<PropertyTag> forbidden = new HashSet<>();
			forbidden.add(StandardProperties.STARTED_PROP);
			forbidden.add(StandardProperties.ENDED_PROP);
			ResolvesWithoutChecker checker = new ResolvesWithoutChecker(getContext(), null, forbidden);
			// maybe we should
			for(PropertyTag t : props.keySet()) {
				PropExpression exp = props.get(t);
				try {
					if( ((PropExpression<?>)exp).accept(checker)) {
						derived.put(t, exp); 
					}
				} catch (Exception e) {
					log.error("Error filtering derived props", e);
				}
			}
		}else{
			log.debug(tag+" parent not a derived property factory");
		}


		
		try{
			derived.put(StandardProperties.STARTED_PROP, AGGREGATE_STARTED_PROP);
			derived.put(StandardProperties.ENDED_PROP,AGGREGATE_ENDED_PROP);
			
		}catch(PropertyCastException e){
			getLogger().error("Failed to make time aliases",e);
		}

		// Add explicit expressions for this table
		derived.addFromProperties(finder,c , tag);

	}
	protected AccessorMap getAccessorMap() {
		return ExpressionCast.getExpressionTargetFactory(this).getAccessorMap();
	}

	/** Create the sum and key property sets.
	 * This needs to be called after the accessormap is fully built.
	 * 
	 */
	private void makePropSets() {
		AccessorMap<AggregateUsageRecordFactory.AggregateRecord> map=getAccessorMap();
		AppContext c = getContext();
		sum_set =new HashSet<>();
		key_set =new HashSet<>();
		Logger log = getLogger();
		if( parent != null ){
			for(PropertyTag t : map.getProperties()){
				log.debug("consider aggregation of "+t.getFullName());
				if( map.writable(t)){

					if( parent.hasProperty(t) && t.getTarget() != Date.class){

						if (Number.class.isAssignableFrom(t.getTarget()) && ! c.getBooleanParameter(getConfigName(t), false)) {
							sum_set.add( t);
							log.debug("sum property added "+t.getFullName());
						}else{
							key_set.add(t);
							log.debug("key property added "+t.getFullName());
						}
					}
				}
			}
		}
	}

	private Number combineNumber(boolean add, Number old, Number val) {
		if( val == null ){
			return old;
		}
		
		if (old instanceof BigInteger) {
			if( add ){
				return ((BigInteger) old).add(BigInteger.valueOf(val.longValue()));
			}else{
				return ((BigInteger) old).subtract(BigInteger.valueOf(val.longValue()));
			}
		} 
		
		if( add ){
			return NumberOp.add(old, val);
		}else{
			return NumberOp.sub(old, val);
		}
	}

	/** Clear cache committing any changes
	 * 
	 * @throws DataFault
	 */
	private void clear() throws DataFault{
		for(int i=0 ; i< CACHE_SIZE ; i++ ){
			if( cache[i] != null ){
				cache[i].commit();
				cache[i].release();
				cache[i]=null;
			}
		}
	}
	public boolean aggregate(ExpressionTargetContainer rec) throws InvalidExpressionException,
			DataException, CannotFilterException, NoSQLFilterException {

		// commit as we go along for safety.
		Use last = aggregateNoCommit(rec,true);
		return last.commit();
	}

	/** Aggregate records but do not commit changes to the cached records unless the
	 * cached object changes
	 * 
	 * @param rec
	 * @throws InvalidPropertyException
	 * @throws DataException
	 * @throws CannotFilterException 
	 * @throws NoSQLFilterException 
	 */
	
	@SuppressWarnings("unchecked")
	private Use aggregateNoCommit(ExpressionTargetContainer rec, boolean add)
			throws InvalidExpressionException, DataException, CannotFilterException, NoSQLFilterException {
		getFinder(); // make sure init has been called
		raw_counter++;
		Date start_point = mapStart(rec.getProperty(start_target_prop));
		Date end_point = mapEnd(rec.getProperty(end_target_prop));
		AggregateRecord target = findTarget(rec, start_point, end_point);
		ExpressionTargetContainer agg = target.getProxy();
		
		// Don't need atomic update as we can re-generate
		for (PropertyTag t : getSumProperties()) {

			Number old = (Number) agg.getProperty(t);
			Number val = (Number) rec.getProperty(t);
			if( val != null ){
				if (old == null) {
					agg.setProperty(t, combineNumber(add,0.0, (Number) rec
							.getProperty(t)));
				} else {
					agg.setProperty(t, combineNumber(add,old, (Number) rec
							.getProperty(t)));
				}
			}

		}
		
		return target;
	}

	public void deAggregate(ExpressionTargetContainer rec) throws InvalidExpressionException,
			DataException, CannotFilterException, NoSQLFilterException {
		Use last = aggregateNoCommit(rec,false);
		last.commit();
		
	}

	public UsageProducer getMaster() {
		if( parent == null) {
			getFinder();
		}
		return parent;
	}
	/** Find an aggregate in the cache or create a new one
	 * 
	 * @param source
	 * @param start_point Aggregate period start
	 * @param end_point   Aggregate period end
	 * @return
	 * @throws DataException
	 * @throws InvalidPropertyException
	 * @throws CannotFilterException 
	 * @throws NoSQLFilterException 
	 */
	private AggregateRecord findTarget(PropertyContainer source,Date start_point, Date end_point) throws DataException, InvalidPropertyException, CannotFilterException, NoSQLFilterException{
		
		for(int i=0;i<CACHE_SIZE;i++){
			if( cache[i] != null && cache[i].matches(source,start_point,end_point)){
				return cache[i];
			}
		}
		if( cache[replace] != null){
			cache[replace].commit();
			cache[replace].release();
			cache[replace]=null;
		}
		fetch_counter++;
		AggregateRecord result = cache[replace] = makeTarget(source,start_point,end_point);
        replace=(replace+1)%CACHE_SIZE;
        assert(result.matches(source, start_point,end_point));
		return result;
	}
	/** Create a new Aggregate records
	 * 
	 * @param source  Source record for key properties
	 * @param start  Aggregate period start
	 * @param end    Aggregate period end
	 * @return
	 * @throws DataException
	 * @throws InvalidPropertyException 
	 * @throws NoSQLFilterException 
	 */
	@SuppressWarnings("unchecked")
	private AggregateRecord makeTarget(PropertyContainer source, Date start,Date end)
			throws DataException, CannotFilterException, InvalidPropertyException, NoSQLFilterException {
		assert(start.before(end));
		SQLAndFilter<AggregateRecord> fil = new SQLAndFilter<>(getTarget());
		AccessorMap map = getAccessorMap();
		fil.addFilter(FilterConverter.convert(map.getFilter(AGGREGATE_STARTED_PROP, null, start)));
		fil.addFilter(FilterConverter.convert(map.getFilter(AGGREGATE_ENDED_PROP, null, end)));
		for(PropertyTag t : getKeyProperties()){
			if( source.supports(t) && hasProperty(t)){
				Object val = source.getProperty(t, null);
				if( val != null){
					fil.addFilter(FilterConverter.convert(map.getFilter(t, null, val)));
				}
			}
		}
		
		AggregateRecord result = find(fil, true);
		if (result == null) {
			result = makeBDO();
			ExpressionTargetContainer proxy = result.getProxy();
			proxy.setProperty(AGGREGATE_STARTED_PROP, start);
			proxy.setProperty(AGGREGATE_ENDED_PROP, end);
			for(PropertyTag t : getKeyProperties()){
				Object val = source.getProperty(t, null);
				if( val != null){
					proxy.setOptionalProperty(t, val);
				}
			}
			//result.commit();
		}
		
		assert(result.matches(source, start, end));
		return result;
	}

	/**
	 * Map a time point to the end of the enclosing aggregation period
	 * The result may equal the input
	 * @param point
	 * @return Date
	 */
	public abstract Date mapEnd(Date point);

	/**
	 * Map a time point to the start of the enclosing aggregation period
     * The result MUST be before the input (start times are the last valid date of 
     * the previous period)
	 * @param point
	 * @return Date
	 */
	public abstract Date mapStart(Date point);

	public void regenerate() throws Exception {
		if(parent == null ){
			return;
		}
		DatabaseService db = getContext().getService(DatabaseService.class);
		FilterDelete<AggregateRecord> del = new FilterDelete<>(res);
		del.delete(null);
		try(CloseableIterator<ExpressionTargetContainer> it = parent.getExpressionIterator(new AndRecordSelector())){
			int i=0;
			while (it.hasNext()) {
				ExpressionTargetContainer rec = it.next();
				// This could be such a large and expensive operation that it is worth
				// supressing the record by record update operations.
				aggregateNoCommit(rec,true);
				rec.release();
				i++;
				if(0 ==  i % 1000 ) {
					db.commitTransaction();
				}
			}
		}
		clear(); // commit final changes
		
	}

	public void fastRegenerate() throws Exception{
		if( parent == null){
			return;
		}
		AndRecordSelector sel = new AndRecordSelector();
		// end_target_prop is correct as regenerate arguments specify end dates.
		Date start = parent.getReduction(new DateReductionTarget(Reduction.MIN, end_target_prop), sel);
		Date end = parent.getReduction(new DateReductionTarget(Reduction.MAX,end_target_prop), sel);
		if( start != null && end != null && end.after(start)){
			regenerate(start, end);
		}
	}
	/** Regenerate all aggregate records where the end date lies between the 
	 * specified dates
	 * 
	 * @param start
	 * @param end
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public void regenerate(Date start, Date end) throws Exception{
		if( parent == null){
			return;
		}
		if( ! end.after(start) ){
			throw new ConsistencyError("date parameters reversed/duplicated");
		}
		Logger log = getLogger();
		start = mapStart(start);
		end = mapEnd(end);
		log.debug("regenerate aggregate range "+start+" to "+end);
		FilterDelete<AggregateRecord> del = new FilterDelete<>(res);
		DatabaseService db = getContext().getService(DatabaseService.class);

		Set<ReductionTarget> targets = new LinkedHashSet<>();
		
		for( PropertyTag<? extends Number> sum_tag : getSumProperties()){
			targets.add(new NumberSumReductionTarget( sum_tag));
		}
		
		
		for( PropertyTag key_tag : getKeyProperties()){
			targets.add(new IndexReduction(key_tag));
		}
		Date p_end=end;
		Date p_start=mapStart(p_end);
		while(p_end.after(start)){
			if( ! p_start.before(p_end)) {
				throw new ConsistencyError("Invalid regenerate period "+p_start+" "+p_end);
			}
			log.debug("Period is "+p_start+" to "+p_end+" epoch "+start);
			AndRecordSelector sel = new AndRecordSelector();
			sel.add(new SelectClause(end_target_prop, MatchCondition.GT, p_start));
			sel.add(new SelectClause(end_target_prop, MatchCondition.LE, p_end));
			
			
			// delete all aggregates with end in the target range
			del.delete(FilterConverter.convert(getFilter(sel))); 
            
			
			// first sum records that start and end within the target period
			// aggreating in the database for speed.
			AndRecordSelector inner_sel = new AndRecordSelector(sel);
			inner_sel.add(new SelectClause(start_target_prop, MatchCondition.GT, p_start));
			
			Map<ExpressionTuple, ReductionMapResult> map = parent.getIndexedReductionMap(targets, inner_sel);
			
			for(ExpressionTuple et : map.keySet()){
				PropertyMap tmap = new PropertyMap();
				for(PropExpression e : et.expressionSet()){
					if( e instanceof PropertyTag){
						Object v = et.get(e);
						tmap.setProperty((PropertyTag)e , v);
					}
				}
				PropertyTuple key = new PropertyTuple(tmap);
				Use agg2 = makeTarget(key, p_start, p_end);
				ExpressionTargetContainer proxy =agg2.getProxy();
				Map<ReductionTarget,Object> data = map.get(et);
				for(ReductionTarget target : data.keySet()){
					proxy.setProperty((PropertyTag)target.getExpression(), data.get(target));
				}
				agg2.commit();
			}
			
			if( ! start_target_prop.equals(end_target_prop)){
				// long records ending in the period need to be aggregated individually
				AndRecordSelector overlap_sel = new AndRecordSelector(sel);
				
				overlap_sel.add(new SelectClause(start_target_prop, MatchCondition.LE, p_start));
				try(CloseableIterator<ExpressionTargetContainer> ov_it = parent.getExpressionIterator(overlap_sel)){
					while(ov_it.hasNext()){
						ExpressionTargetContainer rec = ov_it.next();
						// This could be such a large and expensive operation that it is worth
						// supressing the record by record update operations.
						aggregateNoCommit(rec,true);
						rec.release();
					}
				}
				clear(); // commit cached records
				// flush this part of the transaction.
				db.commitTransaction();
			}
			Date old_start = p_end=p_start;
			p_start=mapStart(p_end);
			
			log.debug("Next Period is "+p_start+" to "+p_end+" epoch "+start);
		}
		
	}

	Set<PropertyTag> getKeyProperties() {
		if( key_set == null ) {
			makePropSets();
		}
		return key_set;
	}

	Set<PropertyTag<? extends Number>> getSumProperties() {
		if( sum_set == null) {
			makePropSets();
		}
		return sum_set;
	}

	@Override
	public Class<AggregateRecord> getTarget() {
		return AggregateRecord.class;
	}

	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new AggregateRecord(this,res);
	}

	
	public long getRawCounter(){
		return raw_counter;
	}
	public long getFetchCounter(){
		return fetch_counter;
	}
	public String endListenerParse() {
		try {
			clear();
		} catch (DataFault e) {
			getLogger().error("Error in clear",e);
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("raw_records=");
		sb.append(getRawCounter());
		sb.append(" aggregates_fetched=");
		sb.append(getFetchCounter());
		sb.append("\n");
		return sb.toString();
	}
	public void postCreate(PropertyContainer props, ExpressionTargetContainer rec)
			throws Exception {
		aggregate(rec);
		
	}
	public void preDelete(ExpressionTargetContainer rec) throws Exception {
		
		deAggregate(rec);
	}
	public void startListenerParse() {
		raw_counter=0;
		fetch_counter=0;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyFactory#addSummaryContent(uk.ac.ed.epcc.webapp.content.ContentBuilder)
	 */
	@Override
	public void addSummaryContent(ContentBuilder hb) {
		hb.addHeading(4,"Match properties");
		ExtendedXMLBuilder xml = hb.getText();
		xml.open("ul");
		for(PropertyTag t : getKeyProperties()){
			xml.open("li");
			xml.clean(t.getName());
			xml.close();
		}
		xml.close();
		xml.appendParent();
		hb.addHeading(4,"Sum properties");
		xml = hb.getText();
		xml.open("ul");
		for(PropertyTag t : getSumProperties()){
			xml.open("li");
			xml.clean(t.getName());
			xml.close();
		}
		xml.close();
		xml.appendParent();
	}
	
	@Override
	public Map<TableTransitionKey, Transition> getTableTransitions() {
		
		Map<TableTransitionKey, Transition> transitions = new LinkedHashMap<>();
		
		
		if( parent_producer != null){
			transitions.put(REGENERATE, new AbstractDirectTransition<AggregateUsageRecordFactory>() {

			public FormResult doTransition(AggregateUsageRecordFactory target,
					AppContext c) throws TransitionException {
				try {
					if( USE_FAST_REGENERATE.isEnabled(getContext())) {
						target.fastRegenerate();
					}else {
						target.regenerate();
					}
				} catch (Exception e) {
					getLogger().error("Error regenerating table",e);
					throw new TransitionException("Regenerate failed");
				}
				return new ViewTableResult(target);
			}
		});
		transitions.put(REGENERATE_RANGE, new AbstractFormTransition<AggregateUsageRecordFactory>() {
			private static final String RANGE = "Range";
			final class RangeAction extends FormAction{
				public RangeAction(AggregateUsageRecordFactory target) {
					super();
					this.target = target;
				}
				private final AggregateUsageRecordFactory target;
				@Override
				public FormResult action(Form f) throws ActionException {
					TimePeriod period = (TimePeriod) f.get(RANGE);
					try {
						target.regenerate(period.getStart(),period.getEnd());
					} catch (Exception e) {
						throw new ActionException("Error in regenerate", e);
					}
					return new ViewTableResult(target);
				}
				
			}
			
			@Override
			public void buildForm(Form f, AggregateUsageRecordFactory target, AppContext conn)
					throws TransitionException {
				f.addInput(RANGE, "Time range to regenerate", new SimplePeriodInput());
				f.addAction("Regenerate", new RangeAction(target));
			}

				
			});	
		}
		return transitions;
	}
	
}