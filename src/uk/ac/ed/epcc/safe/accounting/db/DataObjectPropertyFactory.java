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
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.transitions.TableRegistry;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyFactory;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FalseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.jdbc.table.GeneralTransitionSource;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecificationTransitionSource;
import uk.ac.ed.epcc.webapp.model.data.FieldSQLExpression;
import uk.ac.ed.epcc.webapp.model.data.FieldValue;
import uk.ac.ed.epcc.webapp.model.data.FilterResult;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.filter.FilterUpdate;
import uk.ac.ed.epcc.webapp.model.data.iterator.SkipIterator;
import uk.ac.ed.epcc.webapp.model.data.table.TableStructureDataObjectFactory;
import uk.ac.ed.epcc.webapp.time.Period;

/** Base class for DataObjectFactories that hold accounting properties
 * 
 * @author spb
 *
 * @param <T>
 */
public abstract class DataObjectPropertyFactory<T extends DataObjectPropertyContainer>
		extends TableStructureDataObjectFactory<T> implements ExpressionTargetFactory<T>,
		DerivedPropertyFactory {
	
	
	protected class DataObjectTableRegistry extends TableRegistry{
		public DataObjectTableRegistry(){
			super(getProperties(),getAccessorMap());
			TableSpecification spec = getFinalTableSpecification(getContext(), getTag());
			if(spec != null ){
				addTransitionSource(new TableSpecificationTransitionSource<DataObjectPropertyFactory>(res, spec));
			}
			addTransitionSource(new GeneralTransitionSource<DataObjectPropertyFactory>(res));
		}
	}
	protected DataObjectTableRegistry makeTableRegistry() {
		return new DataObjectTableRegistry();
	}
	


	@Override
	protected Map<String, Object> getSelectors() {
		
		AccessorMap<T> map = getAccessorMap();
		return new HashMap<String,Object>(map.getSelectors());
		
	}
	 /* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.SelectClauseFilterTarget#getSelectClauseFilter(uk.ac.ed.epcc.safe.accounting.SelectClause)
	 */
	public <I> BaseFilter<T> getSelectClauseFilter(SelectClause<I> c) {
		try {
			
			return getAccessorMap().getFilter(c.tag, c.match, c.data);
			
		} catch (CannotFilterException e) {
			getContext().error(e,"Attempt to filter on illegal property");
			return new FalseFilter<T>(getTarget());
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

	

	public final <R> BaseFilter<T> getFilter(PropExpression<R> expr,
			MatchCondition match, R value) throws CannotFilterException{
		return getAccessorMap().getFilter( expr, match, value);
	}
	
	public final <R> BaseFilter<T> getRelationFilter(PropExpression<R> left,
			MatchCondition match, PropExpression<R> right)
			throws CannotFilterException {
		return getAccessorMap().getRelationFilter(left, match, right);
	}

	public final <R> BaseFilter<T> getNullFilter(PropExpression<R> expr,
			boolean is_null) throws CannotFilterException {
		return getAccessorMap().getNullFilter(expr, is_null);
	}
	public BaseFilter<T> getPeriodFilter(Period period,
			PropExpression<Date> start,
			PropExpression<Date> end, OverlapType type, long cutoff)
			throws CannotFilterException {
		return getAccessorMap().getPeriodFilter(period, start, end,type,cutoff);
	}
	
	public <I> OrderFilter<T> getOrderFilter(boolean descending, PropExpression<I> expr)
			throws CannotFilterException {
		return getAccessorMap().getOrderFilter(descending, expr);
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
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(null,this,false);
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
}