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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.transitions.PropertyInfoGenerator;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyFactory;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.GenericBinaryFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableContentProvider;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.FieldSQLExpression;
import uk.ac.ed.epcc.webapp.model.data.FieldValue;
import uk.ac.ed.epcc.webapp.model.data.FilterResult;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.filter.FilterUpdate;
import uk.ac.ed.epcc.webapp.model.data.iterator.SkipIterator;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.session.UnknownRelationshipException;
import uk.ac.ed.epcc.webapp.time.Period;

/** Base class for DataObjectFactories that hold accounting properties
 * 
 * If the property <b><i>config-tag</i>.default_filter_relationship</b> is defined
 * then the corresponding filter is added to any filter created from a {@link RecordSelector} via the {@link #getFilter(RecordSelector)} method.
 * 
 * @author spb
 *
 * @param <T>
 */
public abstract class DataObjectPropertyFactory<T extends DataObjectPropertyContainer>
		extends DataObjectFactory<T> implements ExpressionTargetFactory<T>,
		DerivedPropertyFactory, TableTransitionContributor,TableContentProvider {
	
	
	/** get a set of configuration parameters that configure this object.
	 * This is used to produce a generic configuration table transtition.
	 * 
	 * @return
	 */
	public Set<String> getConfigProperties(){
		return new LinkedHashSet<>();
	}

	public abstract RepositoryAccessorMap<T> getAccessorMap();
	@Override
	protected Map<String, Object> getSelectors() {
		
		RepositoryAccessorMap<T> map = getAccessorMap();
		return new HashMap<String,Object>(map.getSelectors());
		
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
		return DataObjectPropertyContainer.class;
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

	

	/** Get a filter from a {@link RecordSelector}
	 * 
	 * @param selector
	 * @return
	 * @throws CannotFilterException 
	 */
	
	public final BaseFilter<T> getFilter(RecordSelector selector) throws CannotFilterException {
		if( selector == null ){
			return addDefaultFilter(null);
		}
		try {
			return addDefaultFilter(selector.visit(new FilterSelectVisitor<T>(this)));
		}catch(CannotFilterException e){
			throw e;
		} catch (Exception e) {
			throw new CannotFilterException(e);
		}
	}
	/** extension method to augment
	 * 
	 * @param fil {@link BaseFilter} may be null
	 * @return
	 */
	private final BaseFilter<T> addDefaultFilter(BaseFilter<T> fil){
		String default_relationship = getContext().getInitParameter(getConfigTag()+".default_filter_relationship");
		if( default_relationship != null && ! default_relationship.isEmpty()){
			try {
				return new AndFilter<>(getTarget(),getAccessorMap().getRelationshipFilter(default_relationship), fil);
			} catch (CannotFilterException e) {
				getLogger().error("Error adding default relationship", e);
			}
		}
		return fil;
	}
	
	public BaseFilter<T> getRelationshipFilter(String relationship,BaseFilter<T> fallback) throws CannotFilterException {
		try {
			return getContext().getService(SessionService.class).getRelationshipRoleFilter(this, relationship);
		} catch (UnknownRelationshipException e) {
			throw new CannotFilterException(e);
		}
	}
	/** get a {@link FilterResult} directly from a {@link RecordSelector}.
	 * 
	 * @param selector
	 * @return
	 * @throws DataFault
	 * @throws CannotFilterException
	 */
	public final FilterResult<T> getResult(RecordSelector selector) throws DataFault, CannotFilterException {
		return getResult(getFilter(selector));
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

	/**  Perform an update in the database
	 * 
	 * @param <X>
	 * @param tag
	 * @param value
	 * @param sel
	 * @return number of records modified
	 * @throws DataFault 
	 * @throws InvalidPropertyException 
	 * @throws CannotFilterException 
	 */
	@SuppressWarnings("unchecked")
	public final <X> int update(PropertyTag<X> tag, X value, RecordSelector sel) throws DataFault, InvalidPropertyException, CannotFilterException{
		BaseFilter<T> filter = getFilter(sel);
		AccessorMap m = getAccessorMap();
		if( m.hasProperty(tag) && m.writable(tag)){
		try{
			SQLFilter<T> sql_filter = FilterConverter.convert(filter);
			FilterUpdate<T> update = new FilterUpdate<T>(res);
			SQLExpression<X> targ = m.getSQLExpression(tag);
		
			if( targ != null && targ instanceof FieldValue){
			
				return update.update((FieldSQLExpression<X,T>)targ, value, sql_filter);
			}
		}catch(Exception e){
			//do things the hard way
			int count=0;
			for(T ur : new FilterSet(filter)){
				ur.setProperty(tag, value);
				if( ur.commit()){
					count++;
				}
			}
			return count;
		}
		}
		throw new InvalidPropertyException(getConfigTag(),tag);
	}
	/**  Perform an update in the database 
	 * 
	 * 
	 * @param <X>
	 * @param tag
	 * @param value
	 * @param sel
	 * @return number of records modified
	 * @throws DataFault 
	 * @throws CannotFilterException 
	 * @throws InvalidExpressionException 
	 */
	@SuppressWarnings("unchecked")
	public final <X> int update(PropertyTag<X> tag, PropExpression<X> value, RecordSelector sel) throws DataFault, CannotFilterException, InvalidExpressionException{
		BaseFilter<T> filter = getFilter(sel);
		AccessorMap m = getAccessorMap();
		if( m.hasProperty(tag) && m.writable(tag)){
		try{
			SQLFilter<T> sql_filter = FilterConverter.convert(filter);
			FilterUpdate<T> update = new FilterUpdate<T>(res);
			SQLExpression<X> targ = m.getSQLExpression(tag);
			SQLExpression<X> value_expr = m.getSQLExpression(value);
			if( targ != null && targ instanceof FieldValue){
			
				return update.updateExpression((FieldSQLExpression<X,T>)targ, value_expr, sql_filter);
			}
		}catch(Exception e){
			//do things the hard way
			int count=0;
			for(T ur : new FilterSet(filter)){
				ur.setProperty(tag, ur.evaluateExpression(value));
				if( ur.commit()){
					count++;
				}
			}
			return count;
		}
		}
		throw new InvalidPropertyException(getConfigTag(),tag);
	}
	public final T find(RecordSelector sel) throws DataException, CannotFilterException {
		return find(getFilter(sel),true);
	}
	public final  <I> boolean compatible(PropExpression<I> expr) {
		
		return getAccessorMap().resolves(expr,false);
	}
	
	public final boolean compatible(RecordSelector sel){
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(null,getAccessorMap(),false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			return false;
		}
	}
	public final Iterator<T> getIterator(RecordSelector sel) throws DataFault, CannotFilterException {
		return this.new FilterIterator(getFilter(sel));
	}
	
	public  final Iterator<T> getIterator(RecordSelector sel,int skip,int count) throws DataFault, CannotFilterException {
		BaseFilter<T> filter = getFilter(sel);
		try{
			return this.new FilterIterator(FilterConverter.convert(filter),skip,count);
		}catch(NoSQLFilterException e){
			return new SkipIterator<T>(new FilterIterator(filter), skip, count);
		}
	}
	/** Get the number of records matching a {@link RecordSelector}
	 * 
	 */
	public final long getRecordCount(RecordSelector selector)
			throws Exception {
		        return getCount(getFilter(selector));
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.selector.PropertyTargetGenerator#getProperty(uk.ac.ed.epcc.safe.accounting.properties.PropertyTag, java.lang.Object)
	 */
	@Override
	public <X> X getProperty(PropertyTag<X> tag, T record) throws InvalidExpressionException {
		return getAccessorMap().getProxy(record).getProperty(tag);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator#evaluateExpression(uk.ac.ed.epcc.safe.accounting.properties.PropExpression, java.lang.Object)
	 */
	@Override
	public <I> I evaluateExpression(PropExpression<I> expr, T record) throws InvalidExpressionException {
		return getAccessorMap().getProxy(record).evaluateExpression(expr);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.jdbc.table.TableContentProvider#addSummaryContent(uk.ac.ed.epcc.webapp.content.ContentBuilder)
	 */
	@Override
	public void addSummaryContent(ContentBuilder hb) {
		AccessorMap m = getAccessorMap();
		PropertyInfoGenerator gen = new PropertyInfoGenerator(null, m);
		gen.getTableTransitionSummary(hb);
		
		Set<String> configs = getConfigProperties();
		if( configs != null && ! configs.isEmpty()){
			hb.addHeading(3, "Configuration parameters");
			Table t = new Table();
			for(String param : configs){
				t.put("Value",param, getContext().getInitParameter(param, "Not-set"));
			}
			t.setKeyName("Parameter");
			hb.addTable(getContext(), t);
		}
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor#getTableTransitions()
	 */
	@Override
	public Map<TableTransitionKey, Transition<? extends DataObjectFactory>> getTableTransitions() {
		Map<TableTransitionKey, Transition<? extends DataObjectFactory>> map = new LinkedHashMap<TableTransitionKey, Transition<? extends DataObjectFactory>>();
		Set<String> configs = getConfigProperties();
		if( configs != null && ! configs.isEmpty()){
			map.put(new AdminOperationKey( "Configure","Edit configuration parameters directly"), new ConfigTransition(getContext(), configs));
		}
		return map;
	}
}