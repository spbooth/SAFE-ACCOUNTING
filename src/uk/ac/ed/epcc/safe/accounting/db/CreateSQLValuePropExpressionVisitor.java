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
import java.util.LinkedList;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ComparePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConvertMillisecondToDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DoubleCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DoubleDeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationSecondsPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.IntPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LabelPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LongCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.MilliSecondDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.NamePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.expr.SelectPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.StringPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.TypeConverterPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.jdbc.expr.BinaryExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.BinarySQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.CompareSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.CompositeIndexedSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.ConstExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.DateSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.DoubleConvertSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.DurationSecondConvertSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.IndexedSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.IntConvertSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.LabellerSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.LongConvertSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.MillisecondSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLSelectValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.StringConvertSQLValue;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.expr.DurationConvertSQLValue;
import uk.ac.ed.epcc.webapp.model.data.expr.DurationSQLValue;
import uk.ac.ed.epcc.webapp.model.data.expr.TypeConverterSQLValue;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** get an SQLValue from a PropExpression
 * We try to generate an SQLExpression where possible as long as this does not
 * require the introduction of an SQL join.
 * 
 * @author spb
 *
 */
public abstract class CreateSQLValuePropExpressionVisitor implements
		PropExpressionVisitor<SQLValue> {
	
	private static final Feature PARTIAL_JOIN_FEATURE = new Feature("sqlvalue.partial_join",true,"Perform SQL joins to evaluate remote references");
    private final Class target;
	private final AppContext conn;
    public CreateSQLValuePropExpressionVisitor(Class target,AppContext c){
    	this.target=target;
    	conn=c;
    }
    
	
	@SuppressWarnings("unchecked")
	public SQLValue visitStringPropExpression(
			StringPropExpression<?> stringExpression) throws Exception {
		return new StringConvertSQLValue(stringExpression.exp.accept(this));
	}

	@SuppressWarnings("unchecked")
	public SQLValue visitIntPropExpression(
			IntPropExpression<?> intExpression) throws Exception {
		return new IntConvertSQLValue(intExpression.exp.accept(this));
	}
	@SuppressWarnings("unchecked")
	public SQLValue visitLongCastPropExpression(
			LongCastPropExpression<?> intExpression) throws Exception {
		return new LongConvertSQLValue(intExpression.exp.accept(this));
	}
	@SuppressWarnings("unchecked")
	public SQLValue visitDoubleCastPropExpression(
			DoubleCastPropExpression<?> expression) throws Exception {
		return new DoubleConvertSQLValue(expression.exp.accept(this));
	}
	@SuppressWarnings("unchecked")
	public SQLValue visitDurationSecondPropExpression(DurationSecondsPropExpression e) throws Exception{
		return new DurationSecondConvertSQLValue((SQLValue<Duration>) e.getDuration().accept(this));
	}
	@SuppressWarnings("unchecked")
	public SQLValue visitDurationCastPropExpression(
			DurationCastPropExpression<?> expression) throws Exception {
		return new DurationConvertSQLValue(expression.exp.accept(this),expression.resolution);
	}
	@SuppressWarnings("unchecked")
	public final SQLValue visitConstPropExpression(ConstPropExpression<?> constExpression) throws Exception {
		return  new ConstExpression(constExpression.getTarget(),constExpression.val);
	}
	
	
	@SuppressWarnings("unchecked")
	public final SQLValue visitBinaryPropExpression(BinaryPropExpression binaryPropExpression) throws Exception {
		
		SQLValue aa = binaryPropExpression.a.accept(this);
		
		SQLValue bb = binaryPropExpression.b.accept(this);
		
		if( ! Number.class.isAssignableFrom(aa.getTarget())){
			throw new InvalidSQLPropertyException("Non numeric SQL expression");
		}
		if( ! Number.class.isAssignableFrom(bb.getTarget())){
			throw new InvalidSQLPropertyException("Non numeric SQL expression");
		}
		if( aa instanceof SQLExpression && bb instanceof SQLExpression ){
		return  BinaryExpression.create(conn,(SQLExpression)aa, binaryPropExpression.op,
					 (SQLExpression)bb);
		}else{
			return new BinarySQLValue(conn,aa, binaryPropExpression.op, bb);
		}
	}
	

	public SQLValue visitMilliSecondDatePropExpression(
			MilliSecondDatePropExpression milliSecondDate) throws Exception {
		@SuppressWarnings("unchecked")
		SQLValue<Date> de = milliSecondDate.getDateExpression().accept(this);
		if( de instanceof SQLExpression){
			return convertDateExpression((SQLExpression<Date>)de);
		}
		return new MillisecondSQLValue(de);
		
	}

	protected abstract SQLValue convertDateExpression(SQLExpression<Date> de);

	public SQLValue visitConvetMillisecondToDateExpression(
			ConvertMillisecondToDatePropExpression expr) throws Exception {
		@SuppressWarnings("unchecked")
		SQLValue<Number> me = expr.milli_expr.accept(this);
		if( me instanceof SQLExpression){
			return convertMilliExpression((SQLExpression<Number>)me);
		}
		return new DateSQLValue(me);
		
	}

	protected abstract SQLValue convertMilliExpression(SQLExpression<Number> me);


	@SuppressWarnings("unchecked")
	public SQLValue visitNamePropExpression(
			NamePropExpression namePropExpression) throws Exception {
		ReferenceExpression<? extends DataObject> targetRef = namePropExpression.getTargetRef();
		SQLValue a = targetRef.accept(this);
		return new ClassificationSQLValue(conn,target,targetRef.getFactory(conn), a);
	}
	public <T extends DataObject & ExpressionTarget> SQLValue visitDoubleDeRefExpression(
			DoubleDeRefExpression<T, ?> dre) throws Exception {
		if( PARTIAL_JOIN_FEATURE.isEnabled(conn)){
			SQLValue base = dre.getTargetObject().accept(this);
			if( base != null && base instanceof IndexedSQLValue){
				// Consider de-referencing in SQL
				IndexedSQLValue isv = (IndexedSQLValue) base;
				IndexedProducer prod = isv.getFactory(); // base factory for branch
				if( prod instanceof ExpressionTargetFactory ){
					SQLValue branch = ((ExpressionTargetFactory) prod).getAccessorMap().getSQLValue(dre.getNext());
					if( branch != null && branch instanceof IndexedSQLValue){
						return new CompositeIndexedSQLValue(isv, (IndexedSQLValue)branch);
					}
				}
			}
		}
		// This will perform all levels of de-referencing as an evaluation.
		return visitDeRefExpression(dre);
	}
	@SuppressWarnings("unchecked")
	public <T extends DataObject & ExpressionTarget> SQLValue visitDeRefExpression(
			DeRefExpression<T, ?> dre) throws Exception {
		PropExpression<?> expression = dre.getExpression();
		ReferenceExpression<T> x = dre.getTargetObject();
		// SQLValue to create the remote object
		SQLValue<IndexedReference<T>> a =  x.accept(this);
		if( a == null ){
			throw new InvalidSQLPropertyException(x);
		}
		if(  a instanceof IndexedSQLValue ){
			IndexedSQLValue<?,T> dra = (IndexedSQLValue)a;
			
			return new DerefSQLValue(dra, expression, conn);
		}else if( a instanceof DerefSQLValue){
			// We are already evaluating the de-ref in the first step
			// need to split out the IndexedSQLValue and combine the two expressions.
			DerefSQLValue<?,?,IndexedReference<T>> dsv = (DerefSQLValue<?, ?, IndexedReference<T>>) a;
			PropExpression<IndexedReference<T>> ref = dsv.getExpression();
			if( ref instanceof ReferenceExpression){
				if( expression instanceof DeRefExpression){
					return new DerefSQLValue( dsv.getReferenceValue(), new DoubleDeRefExpression(((ReferenceExpression<T>)ref), (ReferenceExpression<?>)expression), conn);
				}else{
					return new DerefSQLValue( dsv.getReferenceValue(), new DeRefExpression(((ReferenceExpression<T>)ref), expression), conn);
				}
			}
		}
		throw new InvalidSQLPropertyException(dre.getTargetObject());
	}

	/** test if the specified expression should be included in the select 
	 * This allows us to skip completely undefined clauses while still detecting valid
	 * clauses that don't resolve as SQLValues
	 * @param e
	 * @return boolean true if expression should be included
	 */
    public abstract <X> boolean includeSelectClause(PropExpression<X> e);
	@SuppressWarnings("unchecked")
	public SQLValue visitSelectPropExpression(SelectPropExpression<?> sel)
			throws Exception {
		LinkedList<SQLValue> list = new LinkedList<SQLValue>();

		for(int i=0;i<sel.length();i++){
			if( includeSelectClause(sel.get(i))){
				// still need to generate an exception if valid clauses can't be
				// made as SQLValue
				SQLValue val = sel.get(i).accept(this);
				if( val != null ){
					list.add(val);
				}

			}
		}
		if( list.size() == 0){
			throw new InvalidSQLPropertyException("No clauses resolvable in select");
		}
		return new SQLSelectValue(sel.getTarget(),list.toArray(new SQLValue[list.size()]));
	}
	@SuppressWarnings("unchecked")
	public SQLValue visitDurationPropExpression(DurationPropExpression sel)
	throws Exception {
		return new DurationSQLValue(sel.start.accept(this), sel.end.accept(this));
	}
	 @SuppressWarnings("unchecked")
		public <T, D> SQLValue visitTypeConverterPropExpression(
				TypeConverterPropExpression<T, D> sel) throws Exception {
		 	assert(sel != null);
			PropExpression<D> innerExpression = sel.getInnerExpression();
			assert(innerExpression!=null);
			return new TypeConverterSQLValue(target,sel.getConverter(), innerExpression.accept(this));
		}


	@SuppressWarnings("unchecked")
	public <T,R> SQLValue<R> visitLabelPropExpression(LabelPropExpression<T,R> expr)
			throws Exception {
		return new LabellerSQLValue<T,R>(conn, expr.getLabeller(), expr.getExpr().accept(this));
	}


	@Override
	public <C extends Comparable> SQLValue visitCompareExpression(
			ComparePropExpression<C> expr) throws Exception {
		
		return new CompareSQLValue<C>(conn, expr.e1.accept(this), expr.m, expr.e2.accept(this));
	}

}