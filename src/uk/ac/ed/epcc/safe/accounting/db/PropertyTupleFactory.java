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
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RelationClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.Tagged;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FalseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.TupleFactory;
import uk.ac.ed.epcc.webapp.model.data.TupleSelfSQLValue;
import uk.ac.ed.epcc.webapp.model.data.iterator.SkipIterator;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** A property enabled {@link TupleFactory}
 * 
 * The component parts are accessible as references.
 * The contributing factories are specified with the property 
 * <b><i>config-tag</i>.members</b>. There is no join clause provided by default this
 * either has to be specified in either:
 * <ul>
 * <li>The report as part of the filter.</li>
 * <li>The class can be extended and {@link #addMandatoryFilter(BaseFilter)} overridden.</li>
 * <li>The filter fan be specified using the config property <b><i>tag</i>.manadatory_filter</b>. This should
 * be a comma separated list of clauses of the form <b>expr=expr<b>.</li>
 * </ul>
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
		
		return (T) new PropertyTuple(this);
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
		 final PropertyTupleFactory fac;
		 public PropertyTuple(PropertyTupleFactory fac) {
			super();
			this.fac=fac; // set first next line will use 
			this.proxy = fac.getAccessorMap().getProxy(this);
			
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
			return fac.getContext();
		}
		
	}

	@Override
	public boolean compatible(RecordSelector sel) {
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(null,getAccessorMap(),false);
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
		String config_spec = getContext().getInitParameter(getTag()+".mandatory_filter");
		if( config_spec != null ) {
			AndFilter<T> and = new AndFilter<>(getTarget(),fil);
			for(String clause : config_spec.split("\\s*,\\s*")) {
				int pos = clause.indexOf('=');
				if( pos < 1 || pos == (clause.length()-1)) {
					getLogger().error("Bad mandatory filter clause "+clause);
					// default to generate nothing on error as a bad filter can
					// result in a very expensive query
					and.addFilter(new FalseFilter<>(getTarget()));
				}else {
					try {
						String exp1=clause.substring(0, pos);
						String exp2=clause.substring(pos+1);
						Parser p = new Parser(getContext(), getFinder());
						PropExpression pe1 = p.parse(exp1);
						PropExpression pe2 = p.parse(exp2);
						and.addFilter(getRawFilter(new RelationClause<>(pe1, pe2)));
					}catch(Throwable t) {
						getLogger().error("Bad mandatory filter clause "+clause,t);
						// default to generate nothing on error as a bad filter can
						// result in a very expensive query
						and.addFilter(new FalseFilter<>(getTarget()));
					}
				}
			}
		}
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
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator#getExpressionTarget(java.lang.Object)
	 */
	@Override
	public ExpressionTargetContainer getExpressionTarget(T record) {
		return getAccessorMap().getProxy(record);
	}

	
	@Override
	public boolean isMyTarget(T record) {
		return record.fac == this;
	}
	
	@Override
	public Class<? super T> getTarget() {
		return PropertyTuple.class;
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
	@Override
	public PropExpressionMap getDerivedProperties() {
		return new PropExpressionMap();
	}
	@Override
	public final Iterator<ExpressionTargetContainer> getExpressionIterator(RecordSelector sel) throws Exception {
		return (Iterator<ExpressionTargetContainer>) getIterator(sel);
	}
}
