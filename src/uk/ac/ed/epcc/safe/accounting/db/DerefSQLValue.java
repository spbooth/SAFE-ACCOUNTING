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

import uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.reference.IndexedTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.FilterProvider;
import uk.ac.ed.epcc.webapp.jdbc.expr.IndexedSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.RemoteSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** An {@link SQLValue} for a {@link DeRefExpression} that implements {@link FilterProvider}
 * The remote object is retrieved and the remote expression evaluated programmatically.
 * @author spb
 * @see SQLExpression
 * @param <H> type of owning object
 * @param <R> type of remote object
 * @param <T> target type of accessor
 */


public class DerefSQLValue<H extends DataObject,R extends DataObject & ExpressionTarget, T> extends
		RemoteSQLValue<H,R, T> implements FilterProvider<H,T>{
	
	protected final PropExpression<T> expr;
	protected final Class<? super H> h_type;
	public DerefSQLValue(IndexedSQLValue<H,R> a, PropExpression<T> expr,
			AppContext conn) throws Exception {
		super(conn, a);
		this.expr = expr;
		this.h_type=a.getFilterType();
		Class r_class=a.getFactory().getTarget();
		// run-time check that this really is an ExpressionTarget
		if( ! ExpressionTarget.class.isAssignableFrom(r_class)){
			throw new PropertyCastException("Class "+r_class.getCanonicalName()+" referenced from DerefExpression does not implement ExpressionTarget");
		}
	}
	

	public PropExpression<T> getExpression(){
		return expr;
	}
	public Class<? super T> getTarget() {
		return expr.getTarget();
	}

	@Override
	public String toString() {
		return getReferenceValue().toString() + "[" + expr.toString() + "]";
	}

	@Override
	public T getRemoteValue(R o) {
		try {
			if (o == null) {
				return null;
			}
			T result = o.evaluateExpression(expr);
			assert(result == null || getTarget().isAssignableFrom(result.getClass()));
			return result;
		} catch (InvalidExpressionException e) {
			getLogger().error("Error evaluating expression",e);
			return null;
		}
	}

	public T getRemoteValueFromNull() {
		
		if( expr.getTarget() == IndexedReference.class){
			// return a null IndexedReference if we can
			// This allows formatters to mark unknown values without marking
			// a real null which might be present in a category total of a table
			PropExpression<T> e = expr;
			while( e instanceof DeRefExpression){
				e = ((DeRefExpression<?,T>)e).getExpression();
			}
			if(e != null && e instanceof IndexedTag){
				return (T) ((IndexedTag)e).makeReference(null);

			}
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public SQLFilter<H> getFilter(MatchCondition match, T val) throws CannotFilterException, NoSQLFilterException {
		
		SQLValue<IndexedReference<R>> v = getReferenceValue();
		if( v instanceof IndexedSQLValue){

			IndexedSQLValue a = (IndexedSQLValue)v;

			IndexedProducer<R> producer;
			try {
				producer = a.getFactory();
			} catch (Exception e) {
				throw new CannotFilterException(e);
			}
			// AccessorMap always can generate filters
			if( producer instanceof ExpressionTargetFactory){
				ExpressionFilterTarget eft = ((ExpressionTargetFactory) producer).getAccessorMap();
				
				return a.getSQLFilter(FilterConverter.convert(eft.getFilter(expr, match, val)));
				
			}
			// Old way producer implements ExpressionFilterTarget directly
			if( producer instanceof ExpressionFilterTarget){
				ExpressionFilterTarget eft = (ExpressionFilterTarget) producer;
				
				return a.getSQLFilter(FilterConverter.convert(eft.getFilter(expr, match, val)));
				
			}
			
			throw new NoSQLFilterException("Target is not an ExpressionFilterTarget");
		}
		throw new NoSQLFilterException("Multiple de-reference");
	}
	@SuppressWarnings("unchecked")
	public SQLFilter<H> getOrderFilter(boolean descending) throws CannotFilterException, NoSQLFilterException {
		
		SQLValue<IndexedReference<R>> v = getReferenceValue();
		if( v instanceof IndexedSQLValue){

			IndexedSQLValue a = (IndexedSQLValue)v;

			IndexedProducer<R> producer;
			try {
				producer = a.getFactory();
			} catch (Exception e) {
				throw new CannotFilterException(e);
			}
			if( producer instanceof ExpressionTargetFactory){
				// Get filter from AccessorMap
				ExpressionFilterTarget eft = ((ExpressionTargetFactory) producer).getAccessorMap();
				
				return a.getSQLFilter(FilterConverter.convert(eft.getOrderFilter(descending, expr)));
				
			}
			// Old way where factory implements directly
			if( producer instanceof ExpressionFilterTarget){
				ExpressionFilterTarget eft = (ExpressionFilterTarget) producer;
				
				return a.getSQLFilter(FilterConverter.convert(eft.getOrderFilter(descending, expr)));
				
			}
			throw new NoSQLFilterException("Target is not an ExpressionFilterTarget");
		}
		throw new NoSQLFilterException("Multiple de-reference");
	}
	@SuppressWarnings("unchecked")
	public SQLFilter<H> getNullFilter(boolean is_null) throws CannotFilterException, NoSQLFilterException {
		SQLValue<IndexedReference<R>> v = getReferenceValue();
		if( v instanceof IndexedSQLValue){
			IndexedSQLValue a = (IndexedSQLValue)v;
			IndexedProducer<R> producer;
			try {
				producer = a.getFactory();
			} catch (Exception e) {
				throw new CannotFilterException(e);
			}
			// Get filters from AccessorMap
			if( producer instanceof ExpressionTargetFactory){
				ExpressionFilterTarget eft = ((ExpressionTargetFactory) producer).getAccessorMap();
				
				return a.getSQLFilter(FilterConverter.convert(eft.getNullFilter(expr, is_null)));
				
			}
			// Ols style where factory implements directly
			if( producer instanceof ExpressionFilterTarget){
				ExpressionFilterTarget eft = (ExpressionFilterTarget) producer;
				
				return a.getSQLFilter(FilterConverter.convert(eft.getNullFilter(expr, is_null)));
				
			}
			throw new NoSQLFilterException("Target is not an ExpressionFilterTarget");
		}
		throw new NoSQLFilterException("Multiple de-reference");
	}


	public Class<? super H> getFilterType() {
		return h_type;
	}


	

}