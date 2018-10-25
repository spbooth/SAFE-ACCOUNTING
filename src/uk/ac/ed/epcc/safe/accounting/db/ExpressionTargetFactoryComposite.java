package uk.ac.ed.epcc.safe.accounting.db;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.transitions.PropertyInfoGenerator;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.table.TableContentProvider;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.Composite;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.iterator.SkipIterator;


/** A {@link Composite} that implements {@link ExpressionTargetFactory}
 * 
 * This is the normal mechanism to property enable a {@link DataObjectFactory}
 * 
 * The behaviour can be modified by having the parent factory or other {@link Composite}s
 * implement {@link AccessorContributer}.
 * 
 * @author spb
 * @see AccessorMap
 * @param <T>
 */
public class ExpressionTargetFactoryComposite<T extends DataObject> extends Composite<T,ExpressionTargetFactoryComposite> implements ExpressionTargetFactory<T>, TableContentProvider {

	public ExpressionTargetFactoryComposite(DataObjectFactory fac) {
		super(fac);
	}
	public static final Feature REPORT_BAD_DERIVATIONS = new Feature("expression_target.report_bad_derivations",true,"Report an error if there is a derivation for a property not in the finder");
	private PropertyFinder reg=null;
	private RepositoryAccessorMap<T> map=null;
	private PropExpressionMap expression_map=null;
	private boolean in_init=false;
	protected final void initAccessorMap() {
		if( in_init == true ){
			throw new ConsistencyError("recursive call to initAccessorMap");
		}
		in_init=true;
		try {
			DataObjectFactory<T> factory = getFactory();
			String table = factory.getConfigTag();
			map = new RepositoryAccessorMap<T>(factory,getRepository());
			MultiFinder finder = new MultiFinder();
			ReferencePropertyRegistry refs = ReferencePropertyRegistry.getInstance(getContext());
			map.makeReferences(refs);
			finder.addFinder(refs);
			PropertyRegistry derived = new PropertyRegistry(table+"DerivedProperties","Derived properties for table "+table);
			expression_map = new PropExpressionMap();
			PropertyRegistry def = new PropertyRegistry(table,"Properties for table "+table);

			if( factory instanceof AccessorContributer) {
				try {
				((AccessorContributer)factory).customAccessors(map, finder, expression_map);
				}catch(Exception t) {
					getLogger().error("Error adding accessors from factory", t);
				}
			}
			for(AccessorContributer contrib : factory.getComposites(AccessorContributer.class)){
				try {
				contrib.customAccessors(map, finder, expression_map);
				}catch(Exception t) {
					getLogger().error("Error adding accessors from composite", t);
				}
			}

			customAccessors(map, finder, expression_map);
			map.populate( finder, def,false);
			finder.addFinder(def);


			expression_map.addFromProperties(derived, finder, getContext(), table);
			
			map.addDerived(getContext(), expression_map);
			finder.addFinder(derived);
			boolean report = REPORT_BAD_DERIVATIONS.isEnabled(getContext());
			// Check all definitions are registered
			for(PropertyTag tag : expression_map.keySet()) {
				if( ! finder.hasProperty(tag)) {
					PropExpression expr = expression_map.get(tag);
					if( report ) {
						getLogger().error("Derived property set for "+tag.getFullName()+"="+expr.toString()+" but not registered in finder of "+getFactory().getTag());
					}
				}
			}
			reg=finder;
		}finally{
			in_init=false;
		}
	}
	/** Extension point to allow custom accessors and registries to be added in sub-classes
	 * 
	 * @param mapi2
	 * @param finder
	 * @param derived
	 */
	protected void customAccessors(AccessorMap<T> mapi2, MultiFinder finder,
			PropExpressionMap derived) {
		
	}
	
	
	public final PropertyFinder getFinder() {
		if( reg == null ){
			initAccessorMap();
			assert(reg != null);
		}
		return reg;
	}

	
	


	

	public void resetStructure() {
		initAccessorMap();
	}

	public final RepositoryAccessorMap<T> getAccessorMap() {
		if( map == null ){
			initAccessorMap();
		}
		return map;
	}



	
	public final PropExpressionMap getDerivedProperties() {
		if( expression_map == null){
			initAccessorMap();
		}
		return expression_map;
	}
	@Override
	public void release() {
		if( map != null){
			map.release();
			map=null;
		}
		reg=null;
		super.release();
	}
	

	@Override
	public boolean compatible(RecordSelector sel) {
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(getLogger(),getAccessorMap(),false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			getLogger().error("Exception checking for compatible selector", e);
			return false;
		}
	}

	@Override
	public CloseableIterator<T> getIterator(RecordSelector sel, int skip, int count) throws Exception {
		DataObjectFactory<T> fac = getFactory();
		BaseFilter<T> filter = map.getFilter(sel);
		try{
			return fac.getResult(FilterConverter.convert(filter),skip,count).iterator();
		}catch(NoSQLFilterException e){
			return new SkipIterator<T>(fac.getResult(filter).iterator(), skip, count);
		}
	}

	@Override
	public CloseableIterator<T> getIterator(RecordSelector sel) throws Exception {
		DataObjectFactory<T> fac = getFactory();
		return fac.getResult(map.getFilter(sel)).iterator();
	}

	@Override
	public long getRecordCount(RecordSelector selector) throws Exception {
		return getFactory().getCount(map.getFilter(selector));
	}
	/** extension method to augment
	 * 
	 * @param fil {@link BaseFilter} may be null
	 * @return
	 */
	private final BaseFilter<T> addDefaultFilter(BaseFilter<T> fil){
		DataObjectFactory<T> fac = getFactory();
		String default_relationship = getContext().getInitParameter(fac.getConfigTag()+".default_filter_relationship");
		if( default_relationship != null && ! default_relationship.isEmpty()){
			try {
				return new AndFilter<>(fac.getTarget(),getAccessorMap().getRelationshipFilter(default_relationship), fil);
			} catch (CannotFilterException e) {
				getLogger().error("Error adding default relationship", e);
			}
		}
		return fil;
	}
	@Override
	public <PT> Set<PT> getValues(PropertyTag<PT> tag, RecordSelector selector) throws Exception {
		if( ! hasProperty(tag)){
			return new HashSet<PT>();
		}
		BaseFilter<T> filter = map.getFilter(selector);	
		try{
			PropertyMaker<T,PT> finder = new PropertyMaker<T,PT>(getAccessorMap(),getRepository(),tag, true);			
			return finder.find(FilterConverter.convert(filter));
		}catch(CannotUseSQLException e){
			Set<PT> result = new HashSet<PT>();
			DataObjectFactory<T> fac = getFactory();
			AccessorMap m = getAccessorMap();
			for(T o : fac.getResult(filter)){
				result.add(map.getProxy(o).getProperty(tag));
			}
			return result;
		}
	}
	@Override
	public <I> boolean compatible(PropExpression<I> expr) {
		return getAccessorMap().resolves(expr,false);
	}
	@Override
	public <P> boolean hasProperty(PropertyTag<P> tag) {
		return getAccessorMap().hasProperty(tag);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.Composite#addSelectors(java.util.Map)
	 */
	@Override
	public Map addSelectors(Map selectors) {
		// Add AccessorMap selectors if not already defined.
		Map<String,Object> add = getAccessorMap().getSelectors();
		for(Entry e : add.entrySet()) {
			if( ! selectors.containsKey(e.getKey())) {
				selectors.put(e.getKey(), e.getValue());
			}
		}
		return selectors;
	}
	@Override
	public void addSummaryContent(ContentBuilder cb) {
		PropertyInfoGenerator gen = new PropertyInfoGenerator(null, getAccessorMap());
		gen.getTableTransitionSummary(cb);
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
		return getFactory().isMine(record);
	}
	@Override
	protected Class<? super ExpressionTargetFactoryComposite> getType() {
		return ExpressionTargetFactoryComposite.class;
	}
	@Override
	public CloseableIterator<ExpressionTargetContainer> getExpressionIterator(RecordSelector sel) throws Exception {
		return new ProxyIterator<T>(this, getIterator(sel));
	}
}
