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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.transitions.PropertyInfoGenerator;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.jdbc.table.TableContentProvider;
import uk.ac.ed.epcc.webapp.model.ClassificationFactory;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.iterator.SkipIterator;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.Period;

public abstract class PropertyTargetClassificationFactory<T extends AccountingClassification> extends
		ClassificationFactory<T> implements ExpressionTargetFactory<T>, TableContentProvider{
	public PropertyTargetClassificationFactory(AppContext ctx, String homeTable) {
		super(ctx, homeTable);
	}
	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new AccountingClassification(this, res);
	}
	@Override
	public Class<? super T> getTarget(){
		return AccountingClassification.class;
	}
	

	
	public <P> boolean hasProperty(PropertyTag<P> tag){
		return getAccessorMap().hasProperty(tag);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.PropertyFilterTarget#getFilter(uk.ac.ed.epcc.safe.accounting.expr.PropExpression, uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition, R)
	 */
	public <R> BaseFilter<T> getFilter(PropExpression<R> expr,MatchCondition match, R value) throws CannotFilterException{
		return getAccessorMap().getFilter( expr, match, value);
	}
   
	public <R> BaseFilter<T> getRelationFilter(PropExpression<R> left,
			MatchCondition match, PropExpression<R> right)
			throws CannotFilterException {
		return getAccessorMap().getRelationFilter(left, match, right);
	}
	public <R> BaseFilter<T> getNullFilter(PropExpression<R> expr,
			boolean is_null) throws CannotFilterException {
		return getAccessorMap().getNullFilter(expr,is_null);
	}
	public BaseFilter<T> getPeriodFilter(Period period,
			PropExpression<Date> start,
			PropExpression<Date> end,
			OverlapType type, long cutoff)
			throws CannotFilterException {
		return getAccessorMap().getPeriodFilter(period, start, end,type,cutoff);
	}
	
	public <I> SQLFilter<T> getOrderFilter(boolean descending, PropExpression<I> expr)
			throws CannotFilterException {
		return getAccessorMap().getOrderFilter(descending, expr);
	}
	public <R> SQLExpression<R> getSQLExpression(PropExpression<R> exp) throws InvalidSQLPropertyException{
		return getAccessorMap().getSQLExpression(exp);
	}


	
	
	

	

	public <I> boolean compatible(PropExpression<I> expr) {
		return getAccessorMap().resolves(expr,false);
	}
	
	/** Get a filter from a {@link RecordSelector}
	 * 
	 * @param selector
	 * @return
	 * @throws CannotFilterException 
	 */
	
	protected BaseFilter<T> getFilter(RecordSelector selector) throws CannotFilterException {
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
	public final boolean compatible(RecordSelector sel){
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(null,getAccessorMap(),false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			return false;
		}
	}
	public  Iterator<T> getIterator(RecordSelector sel) throws DataFault, CannotFilterException {
		return this.new FilterIterator(getFilter(sel));
	}
	
	public  Iterator<T> getIterator(RecordSelector sel,int skip,int count) throws DataFault, CannotFilterException {
		BaseFilter<T> filter = getFilter(sel);
		try{
			return this.new FilterIterator(FilterConverter.convert(filter),skip,count);
		}catch(NoSQLFilterException e){
			return new SkipIterator<T>(new FilterIterator(filter), skip, count);
		}
	}
	public long getRecordCount(RecordSelector selector)
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
	public void addSummaryContent(ContentBuilder hb) {
		AccessorMap m = getAccessorMap();
		PropertyInfoGenerator gen = new PropertyInfoGenerator(null, m);
		gen.getTableTransitionSummary(hb);
		
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
}