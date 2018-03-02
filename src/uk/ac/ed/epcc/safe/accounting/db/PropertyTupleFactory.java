package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.Tagged;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseSQLFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.TupleFactory;
import uk.ac.ed.epcc.webapp.model.data.TupleSelfSQLValue;
import uk.ac.ed.epcc.webapp.model.data.iterator.SkipIterator;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.time.Period;
/** A property enabled {@link TupleFactory}
 * 
 * The component parts are accessible as references.
 * The contributing factories are specified with the property 
 * <b><i>config-tag</i>.members</b>. There is no join clause provided by default this
 * either has to be specified in the report as part of the filter or the class can be extended
 * and {@link #addMandatoryFilter(BaseFilter)} overridden.
 * 
 * @author spb
 *
 * @param <A>
 * @param <AF>
 * @param <T>
 */
public class PropertyTupleFactory<A extends DataObject, AF extends DataObjectFactory<A>,T extends PropertyTupleFactory.PropertyTuple<A>>
extends TupleFactory<A,AF,T> 
implements ExpressionTargetFactory<T>, 
Tagged{

	private static final String MEMBERS_CONFIG_SUFFIX = ".members";
	protected final TupleAccessorMap map;
	private final String tag;
	protected MultiFinder finder = new MultiFinder();
	private LinkedList<ReferenceTag<A,AF>> member_tags;
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.TupleFactory#makeTuple()
	 */
	@Override
	public T makeTuple() {
		
		return (T) new PropertyTuple(getContext(),map);
	}
	public PropertyTupleFactory(AppContext c,String config_tag) {
		super(c);
		this.tag=config_tag;
		addMembers(c, config_tag);
		if( ! hasMemberFactories()) {
			getLogger().error("PropertyTupleFactory "+tag+" has no members");
		}
		map = new TupleAccessorMap(this, config_tag);
		finder.addFinder(StandardProperties.time); // for JobCount
		member_tags = new LinkedList<>();
		ReferencePropertyRegistry refs = ReferencePropertyRegistry.getInstance(c);
		finder.addFinder(refs);
		for(AF fac : getMemberFactories()){
			ReferenceTag<A, AF> tag = (ReferenceTag<A, AF>) refs.find(IndexedReference.class,fac.getTag());
			if( tag != null ){
				member_tags.add(tag);
				map.put(tag, new TupleSelfSQLValue<A,AF,T>(this, fac));
			}
		}
	}
	/**
	 * @param c
	 * @param config_tag
	 */
	protected void addMembers(AppContext c, String config_tag) {
		String nested = c.getInitParameter(config_tag+MEMBERS_CONFIG_SUFFIX);
		if( nested != null){
			for(String tag : nested.split("\\s*,\\s*")){
				AF fac = (AF) c.makeObject(DataObjectFactory.class, tag);
				addFactory(fac);
			}
		}
	}

	public static class PropertyTuple<A extends DataObject> extends TupleFactory.Tuple<A> implements ExpressionTarget,Contexed{
		private final AppContext conn;
		 public PropertyTuple(AppContext conn,TupleAccessorMap map) {
			super();
			this.conn=conn;  // set first next line will use
			this.proxy = map.getProxy(this);
			
		}

		protected final ExpressionTargetContainer proxy;

		@Override
		public <R> R getProperty(PropertyTag<R> tag, R def) {
			return proxy.evaluateExpression(tag, def);
		}

		@Override
		public Parser getParser() {
			return proxy.getParser();
		}

		@Override
		public <T> T evaluateExpression(PropExpression<T> expr) throws InvalidExpressionException {
			return proxy.evaluateExpression(expr);
		}

		@Override
		public <T> T evaluateExpression(PropExpression<T> expr, T def) {
			return proxy.evaluateExpression(expr, def);
		}

		@Override
		public AppContext getContext() {
			return conn;
		}
		
	}

	@Override
	public boolean compatible(RecordSelector sel) {
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(null,this,false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			return false;
		}
	}
	
	/** Augment the filter generated from the {@link RecordSelector} with a standard set of additional filter
	 * 
	 * This is to add a mandatory set of filters to restrict the join.
	 * 
	 * @param fil
	 * @return
	 */
	protected BaseFilter<T> addMandatoryFilter(BaseFilter<T> fil){
		return fil;
	}
	protected final BaseFilter<T> getFilter(RecordSelector selector) throws CannotFilterException {
		return addMandatoryFilter(getRawFilter(selector));
	}
	protected final BaseFilter<T> getRawFilter(RecordSelector selector) throws CannotFilterException {
		if( selector == null ){
			return null;
		}
		try {
			return selector.visit(new FilterSelectVisitor<T>(this));
		}catch(CannotFilterException e){
			throw e;
		} catch (Exception e) {
			throw new CannotFilterException(e);
		}
	}
	@Override
	public Iterator<T> getIterator(RecordSelector sel, int skip, int count) throws Exception {
		BaseFilter<T> filter = getFilter(sel);
		try{
			return this.new TupleIterator(FilterConverter.convert(filter),skip,count);
		}catch(NoSQLFilterException e){
			return new SkipIterator<T>(new TupleIterator(filter), skip, count);
		}
	}

	@Override
	public Iterator<T> getIterator(RecordSelector sel) throws Exception {
		return makeResult(getFilter(sel)).iterator();
	}

	@Override
	public long getRecordCount(RecordSelector selector) throws Exception {
		
		return getCount(getFilter(selector));
	}

	@Override
	public final <PT> Set<PT> getValues(PropertyTag<PT> tag, RecordSelector selector) throws DataException, InvalidExpressionException, CannotFilterException {
		if( ! hasProperty(tag)){
			return new HashSet<PT>();
		}
		BaseFilter<T> filter = getFilter(selector);	
		try{
			TuplePropertyMaker<T,PT> finder = new TuplePropertyMaker<T,PT>(map,this,tag, true);			
			return finder.find(FilterConverter.convert(filter));
		}catch(CannotUseSQLException e){
			Set<PT> result = new HashSet<PT>();
			for(T o : makeResult(filter)){
				result.add(o.evaluateExpression(tag));
			}
			return result;
		}
		
	}

	@Override
	public PropertyFinder getFinder() {
		
		
		return finder;
	}

	@Override
	public <P> boolean hasProperty(PropertyTag<P> tag) {
		return map.hasProperty(tag);
	}

	@Override
	public <I> boolean compatible(PropExpression<I> expr) {

		return map.resolves(expr, false);
	}

	@Override
	public Class<? super T> getTarget() {
		return PropertyTuple.class;
	}

	@Override
	public BaseFilter getFilter(PropExpression expr, MatchCondition match, Object value) throws CannotFilterException {
		return map.getFilter(expr, match, value);
	}

	@Override
	public BaseFilter getNullFilter(PropExpression expr, boolean is_null) throws CannotFilterException {
		return map.getNullFilter(expr, is_null);
	}

	@Override
	public BaseFilter getRelationFilter(PropExpression left, MatchCondition match, PropExpression right)
			throws CannotFilterException {
		return map.getRelationFilter(left, match, right);
	}

	@Override
	public BaseFilter getPeriodFilter(Period period, PropExpression start_prop, PropExpression end_prop,
			OverlapType type, long cutoff) throws CannotFilterException {
		return map.getPeriodFilter(period, start_prop, end_prop, type, cutoff);
	}

	@Override
	public BaseSQLFilter getOrderFilter(boolean descending, PropExpression expr) throws CannotFilterException {
		return map.getOrderFilter(descending, expr);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget#getRelationshipFilter(java.lang.String)
	 */
	@Override
	public BaseFilter getRelationshipFilter(String relationship) throws CannotFilterException {
		return map.getRelationshipFilter(relationship);
	}
	@Override
	public final String getTag() {
		return tag;
	}
	
	protected Collection<ReferenceTag<A,AF>> getMemberTags(){
		return (Collection<ReferenceTag<A,AF>>) member_tags.clone();
	}
	@Override
	public AccessorMap<T> getAccessorMap() {
		// TODO Auto-generated method stub
		return map;
	}
}
