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
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.transitions.TableRegistry;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
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
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.GenericBinaryFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.LinkManager;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.iterator.SkipIterator;
import uk.ac.ed.epcc.webapp.model.data.table.TableStructureLinkManager;
import uk.ac.ed.epcc.webapp.time.Period;

/** A LinkManager that also supports properties.
 * 
 * @author spb
 *
 * @param <T>
 * @param <L>
 * @param <R>
 */
public abstract class AccountingLinkManager<T extends AccountingLinkManager.PropertyTargetLink<L,R>,L extends DataObject,R extends DataObject> extends TableStructureLinkManager<T, L, R> 
implements ExpressionTargetFactory<T>{

	protected AccountingLinkManager(AppContext c, String table,
			DataObjectFactory<L> left_fac, String left_field,
			DataObjectFactory<R> right_fac, String right_field) {
		super(c, table, left_fac, left_field, right_fac, right_field);
	}
	public class AccountingLinkManagerTableRegistry extends TableRegistry{

		public AccountingLinkManagerTableRegistry() {
			super(res,getDefaultTableSpecification(getContext(), getTag(),getLeftFactory(),getLeftField(),getRightFactory(),getRightField()),getProperties(),getAccessorMap());
		}
	}
	protected TableRegistry makeTableRegistry() {
		return new AccountingLinkManagerTableRegistry();
	}
	

public abstract static class PropertyTargetLink<L extends DataObject, R extends DataObject> extends LinkManager.Link<L,R> implements ExpressionTargetContainer{
		private final ExpressionTargetContainer proxy;
		@SuppressWarnings("unchecked")
		protected PropertyTargetLink(AccountingLinkManager<?,L,R> man, Record res) {
			super(man, res);
			AccessorMap map = man.getAccessorMap();
			proxy = map.getProxy(this);
		}
		@SuppressWarnings("unchecked")
		protected AccountingLinkManager<PropertyTargetLink<L,R>, L, R> getFac(){
			return (AccountingLinkManager<PropertyTargetLink<L, R>, L, R>) getLinkManager();
		}
		public final <T> T getProperty(PropertyTag<T> tag) throws InvalidExpressionException {
			return proxy.getProperty(tag);
		}
		public final <T> T getProperty(PropertyTag<T> tag, T def) {
			return proxy.getProperty(tag, def);
		}
		public final <T> T evaluateExpression(PropExpression<T> expr)
		throws InvalidExpressionException {
			return proxy.evaluateExpression(expr);
		}
		public final <T> T evaluateExpression(PropExpression<T> expr,T def){
					return proxy.evaluateExpression(expr,def);
		}
		public final <T> void setProperty(PropertyTag<? super T> tag, T value) throws InvalidPropertyException {
			proxy.setProperty(tag, value);
		}
		public final <T> void setOptionalProperty(PropertyTag<? super T> tag, T value) {
			proxy.setOptionalProperty(tag, value);
		}
		public final <T> void setProperty(PropertyTag<T> tag, PropertyContainer map) throws InvalidExpressionException{
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
		public Set<PropertyTag> getDefinedProperties() {
			return proxy.getDefinedProperties();
		}
		public void setAll(PropertyContainer source) {
			proxy.setAll(source);
		}
		public Parser getParser() {
			return proxy.getParser();
		}
		
	}

private PropertyFinder reg=null;
private AccessorMap<T> map=null;
private PropExpressionMap expression_map=null;

protected final void initAccessorMap(AppContext c, String table) {
	map = new AccessorMap<T>(getTarget(),res,table);
	MultiFinder finder = new MultiFinder();
	ReferencePropertyRegistry refs = ReferencePropertyRegistry.getInstance(c);
	map.makeReferences(refs);
	finder.addFinder(refs);
	PropertyRegistry derived = new PropertyRegistry(table+"DerivedProperties","Derived properties for table "+table);
	expression_map = new PropExpressionMap();
	PropertyRegistry def = new PropertyRegistry(table,"Properties for table "+table);

	customAccessors(map, finder, expression_map);
	map.populate( finder, def,false);
	finder.addFinder(def);
	
	
	expression_map.addFromProperties(derived, finder, c, table);
	map.addDerived(c, expression_map);
	finder.addFinder(derived);
	
	reg=finder;
}
/** Extension point to allow custom accessors and registries to be added.
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
		initAccessorMap(getContext(), getTag());
	}
	return reg;
}







public void resetStructure() {
	super.resetStructure();
	initAccessorMap(getContext(), getConfigTag());
}

public final AccessorMap<T> getAccessorMap() {
	if( map == null ){
		initAccessorMap(getContext(), getTag());
	}
	return map;
}




public final PropExpressionMap getDerivedProperties() {
	if( expression_map == null){
		initAccessorMap(getContext(), getTag());
	}
	return expression_map;
}	



	@Override
	protected Map<String, Object> getSelectors() {
		
		AccessorMap<T> map = getAccessorMap();
		return map.getSelectors();
		
	}
	 /* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.SelectClauseFilterTarget#getSelectClauseFilter(uk.ac.ed.epcc.safe.accounting.SelectClause)
	 */
	public <I> BaseFilter<T> getSelectClauseFilter(SelectClause<I> c) {
		try {
			
			return getAccessorMap().getFilter(c.tag, c.match, c.data);
			
		} catch (CannotFilterException e) {
			getLogger().error("Attempt to filter on illegal property",e);
			return new GenericBinaryFilter<T>(getTarget(),false);
		}
	}
	
	

	
	/**
	 * Get the set of <em>Defined</em> properties.
	 * 
	 * @return Set<PropertyTag>
	 */
	public final Set<PropertyTag> getProperties() {
		return getAccessorMap().getProperties();
	}

	@Override
	public Class<? super T> getTarget() {
		return PropertyTargetLink.class;
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

	

	public final <X> BaseFilter<T> getFilter(PropExpression<X> expr,
			MatchCondition match, X value) throws CannotFilterException{
		return getAccessorMap().getFilter( expr, match, value);
	}
	 public <X> BaseFilter<T> getNullFilter(PropExpression<X> expr,
			boolean is_null) throws CannotFilterException {
		return getAccessorMap().getNullFilter(expr, is_null);
	}

	
	public <Q> BaseFilter<T> getRelationFilter(PropExpression<Q> left,
			MatchCondition match, PropExpression<Q> right)
			throws CannotFilterException {
		return getAccessorMap().getRelationFilter(left, match, right);
	}

	public BaseFilter<T> getPeriodFilter(Period period,
			PropExpression<Date> start, 
			PropExpression<Date> end, OverlapType type,long cutoff)
			throws CannotFilterException {
		return getAccessorMap().getPeriodFilter(period, start, 
				end,type,cutoff);
	}
	public <I> SQLFilter<T> getOrderFilter(boolean descending, PropExpression<I> expr)
			throws CannotFilterException {
		return getAccessorMap().getOrderFilter(descending, expr);
	}

	public <I> boolean compatible(PropExpression<I> expr) {
		return getAccessorMap().resolves(expr,false);
	}

	
	public final boolean compatible(RecordSelector sel){
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(null,this,false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			return false;
		}
	}
	/** Get a filter from a {@link RecordSelector}
	 * 
	 * @param selector
	 * @return
	 * @throws CannotFilterException 
	 */
	
	protected final BaseFilter<T> getFilter(RecordSelector selector) throws CannotFilterException {
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
	public final Iterator<T> getIterator(RecordSelector sel) throws DataFault, CannotFilterException {
		return this.new FilterIterator(getFilter(sel));
	}
	
	public final Iterator<T> getIterator(RecordSelector sel,int skip,int count) throws DataFault, CannotFilterException {
		BaseFilter<T> filter = getFilter(sel);
		try{
			return this.new FilterIterator(FilterConverter.convert(filter),skip,count);
		}catch(NoSQLFilterException e){
			return new SkipIterator<T>(new FilterIterator(filter), skip, count);
		}
	}
	public final long getRecordCount(RecordSelector selector)
			throws Exception {
		        return getCount(getFilter(selector));
	}
	public final <PT> Set<PT> getValues(PropertyTag<PT> tag, RecordSelector selector) throws DataException, InvalidExpressionException, CannotFilterException {
		if( ! hasProperty(tag)){
			return new HashSet<PT>();
		}
		BaseFilter<T> filter = getFilter(selector);	
		try{
			PropertyMaker<T,PT> finder = new PropertyMaker<T,PT>(getAccessorMap(),res,tag, true);			
			return finder.find(FilterConverter.convert(filter));
		}catch(CannotUseSQLException e){
			Set<PT> result = new HashSet<PT>();
			for(T o : new FilterSet(filter)){
				result.add(o.getProperty(tag));
			}
			return result;
		}
		
	}
	
	public final T find(RecordSelector sel) throws DataException, CannotFilterException {
		return find(getFilter(sel));
	}
	@Override
	public void release() {
		if( map != null){
			map.release();
			map=null;
		}
		reg=null;
		if(expression_map != null){
			expression_map.clear();
			expression_map=null;
		}
		super.release();
	}
	
}