package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.*;
import uk.ac.ed.epcc.safe.accounting.reference.ConfigPropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RelationClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.Tagged;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FalseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.TupleFactory;
import uk.ac.ed.epcc.webapp.model.data.TupleSelfSQLValue;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
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
		
		finder.addFinder(StandardProperties.time); // for JobCount
		member_tags = new LinkedList<>();
		ReferencePropertyRegistry refs = ReferencePropertyRegistry.getInstance(c);
		finder.addFinder(refs);
		// This allows us to define custom tags that can be populated as derived properties later
		finder.addFinder(new ConfigPropertyRegistry(c, config_tag));
		map = new TupleAccessorMap(this, config_tag,finder);
		for(AF fac : getMemberFactories()){
			ReferenceTag<A, AF> tag = (ReferenceTag<A, AF>) refs.find(IndexedReference.class,fac.getTag());
			if( tag != null ){
				member_tags.add(tag);
				map.put(tag, new TupleSelfSQLValue<>(this, fac));
			}
		}
		// Allow derived properties to be set for references (and time) properties.
		PropExpressionMap derived = new PropExpressionMap();
		customAccessors(map, finder, derived);
		derived.addFromProperties(finder, c, config_tag);
		map.addDerived(c, derived);
	}
	protected  void customAccessors(AccessorMap<T> mapi2,
			MultiFinder finder, PropExpressionMap derived) {
		
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
				if( fac != null) {
					addFactory(fac);
				}else {
					getLogger().error("No member factory generated for tuple tag "+config_tag+"->"+tag);
				}
			}
		}
	}

	public static class PropertyTuple<A extends DataObject> extends TupleFactory.Tuple<A> implements ExpressionTargetContainer,Contexed{
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

		@Override
		public boolean supports(PropertyTag<?> tag) {
			return proxy.supports(tag);
		}

		@Override
		public boolean writable(PropertyTag<?> tag) {
			return proxy.writable(tag);
		}

		@Override
		public <T> T getProperty(PropertyTag<T> key) throws InvalidExpressionException {
			return proxy.getProperty(key);
		}

		@Override
		public <T> void setProperty(PropertyTag<? super T> key, T value) throws InvalidPropertyException {
			proxy.setProperty(key, value);
		}

		@Override
		public <T> void setOptionalProperty(PropertyTag<? super T> key, T value) {
			proxy.setOptionalProperty(key, value);
			
		}

		@Override
		public Set<PropertyTag> getDefinedProperties() {
			return proxy.getDefinedProperties();
		}

		@Override
		public void setAll(PropertyContainer source) {
			proxy.setAll(source);
		}

		@Override
		public void release() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean commit() throws DataFault {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean delete() throws DataFault {
			// TODO Auto-generated method stub
			return false;
		}
		
	}

	@Override
	public boolean compatible(RecordSelector sel) {
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(getLogger(),getAccessorMap(),false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			getLogger().error("Error checking compatible", e);
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
	protected  BaseFilter<T> addMandatoryFilter(BaseFilter<T> fil){
		TupleAndFilter and = new TupleAndFilter(fil);
		String config_spec = getContext().getInitParameter(getTag()+".mandatory_filter");
		if( config_spec != null ) {
			
			for(String clause : config_spec.split("\\s*,\\s*")) {
				int pos = clause.indexOf('=');
				if( pos < 1 || pos == (clause.length()-1)) {
					getLogger().error("Bad mandatory filter clause "+clause);
					// default to generate nothing on error as a bad filter can
					// result in a very expensive query
					and.addFilter(new FalseFilter<>());
				}else {
					try {
						String exp1=clause.substring(0, pos);
						String exp2=clause.substring(pos+1);
						Parser p = new Parser(getContext(), getFinder());
						PropExpression pe1 = p.parse(exp1);
						PropExpression pe2 = p.parse(exp2);
						and.addFilter(getAccessorMap().getRawFilter(new RelationClause<>(pe1, pe2)));
					}catch(Exception t) {
						getLogger().error("Bad mandatory filter clause "+clause,t);
						// default to generate nothing on error as a bad filter can
						// result in a very expensive query
						and.addFilter(new FalseFilter<>());
					}
				}
			}
		}
		
		return and;
	}
	protected final BaseFilter<T> getFilter(RecordSelector selector) throws CannotFilterException {
		// map will call back to this class
		// to mutate the selector and add mandatory filter
		return map.getFilter(selector);
	}
	
	protected RecordSelector mutateSelector(RecordSelector selector) {
		return selector;
	}
	@Override
	public CloseableIterator<T> getIterator(RecordSelector sel, int skip, int count) throws Exception {
		BaseFilter<T> filter = getFilter(sel);
		try{
			return this.new TupleIterator(FilterConverter.convert(filter),skip,count);
		}catch(NoSQLFilterException e){
			return new SkipIterator<>(new TupleIterator(filter), skip, count);
		}
	}

	@Override
	public CloseableIterator<T> getIterator(RecordSelector sel) throws Exception {
		return makeResult(getFilter(sel)).iterator();
	}

	@Override
	public long getRecordCount(RecordSelector selector) throws Exception {
		
		return getCount(getFilter(selector));
	}
	@Override
	public boolean exists(RecordSelector selector) throws Exception {
		
		return exists(getFilter(selector));
	}
	@Override
	public final <PT> Set<PT> getValues(PropExpression<PT> tag, RecordSelector selector) throws DataException, InvalidExpressionException, CannotFilterException {
		if( ! compatible(tag)){
			return new HashSet<>();
		}
		BaseFilter<T> filter = getFilter(selector);	
		try{
			TuplePropertyMaker<T,PT> finder = new TuplePropertyMaker<T,PT>(map,this,tag, true);			
			return finder.find(FilterConverter.convert(filter));
		}catch(CannotUseSQLException e){
			Set<PT> result = new HashSet<>();
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
	public <P> boolean writable(PropertyTag<P> tag) {
		return map.writable(tag);
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
		return record;
	}

	
	@Override
	public boolean isMyTarget(T record) {
		return record.fac == this;
	}
	
	

	
	@Override
	public final String getTag() {
		return tag;
	}
	
	protected Collection<ReferenceTag<A,AF>> getMemberTags(){
		return (Collection<ReferenceTag<A,AF>>) member_tags.clone();
	}
	@Override
	public TupleAccessorMap getAccessorMap() {
		// TODO Auto-generated method stub
		return map;
	}
	@Override
	public PropExpressionMap getDerivedProperties() {
		return map.getDerivedProperties();
	}
	@Override
	public final CloseableIterator<ExpressionTargetContainer> getExpressionIterator(RecordSelector sel) throws Exception {
		CloseableIterator it = getIterator(sel);
		return it;
	}

}
