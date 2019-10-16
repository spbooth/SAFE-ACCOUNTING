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

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ArrayFuncPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ComparePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstReferenceExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConvertMillisecondToDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DoubleCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DoubleDeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationSecondsPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.IntPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LabelPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LocatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LongCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.MilliSecondDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.NamePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.expr.SelectPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.StringPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.TypeConverterPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.jdbc.DatabaseService;
import uk.ac.ed.epcc.webapp.jdbc.SQLContext;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataError;
import uk.ac.ed.epcc.webapp.jdbc.expr.ArrayFuncExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.BinaryExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.CastDoubleSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.CastLongSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.CompareSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.ConstExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.DateDerefSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.DateSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.DerefSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.IndexedSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.LocateSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.RoundSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.StringConvertSQLExpression;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.expr.DurationSQLExpression;
/** get an {@link SQLExpression} from a {@link PropExpression}
 * 
 * @author spb
 *
 */
public abstract class CreateSQLExpressionPropExpressionVisitor implements
		PropExpressionVisitor<SQLExpression> {
	
	private final AppContext conn;
	private final SQLContext sql;
	 private final Class target; // target class for filters
	public CreateSQLExpressionPropExpressionVisitor(Class target,AppContext c){
		this.target=target;
		this.conn=c;
		DatabaseService db_service = conn.getService(DatabaseService.class);
		try {
			sql=db_service.getSQLContext();
		} catch (SQLException e) {
			db_service.logError("Error making SQLContext", e);
			throw new DataError("Error making SQLContext",e);
		}
	}
    
	
	@SuppressWarnings("unchecked")
	public SQLExpression visitStringPropExpression(
			StringPropExpression<?> stringExpression) throws Exception {
		Class<?> target = stringExpression.exp.getTarget();
		if( target == String.class){
			return stringExpression.exp.accept(this);
		}
		if( Number.class.isAssignableFrom(target)){
			// SQL can treat numbers as strings
			return new StringConvertSQLExpression(stringExpression.exp.accept(this));
		}
		throw new InvalidSQLPropertyException("StringPropExpression not representable as SQLExpression");
		
	}

	public SQLExpression visitIntPropExpression(
			IntPropExpression<?> intExpression) throws Exception {
		// use round if nested expression is a number
		Class<?> target = intExpression.exp.getTarget();
		if( Number.class.isAssignableFrom(target)){
			SQLExpression inner_sql_expr = intExpression.exp.accept(this);
			if( target == Integer.class || inner_sql_expr.getTarget() == Integer.class){
				return inner_sql_expr;
			}
			
			return new RoundSQLExpression(inner_sql_expr);
		}
		throw new InvalidSQLPropertyException("IntPropExpression not representable as SQLExpression");
		
	}
	public SQLExpression visitLongCastPropExpression(
			LongCastPropExpression<?> intExpression) throws Exception {
		// use round if nested expression is a number
		Class<?> target = intExpression.exp.getTarget();
		if( Number.class.isAssignableFrom(target)){
			if( target == Long.class ){
				return intExpression.exp.accept(this);
			}
			return new CastLongSQLExpression<>(intExpression.exp.accept(this));
		}
		throw new InvalidSQLPropertyException("LongPropExpression not representable as SQLExpression");
		
	}
	public SQLExpression visitDoubleCastPropExpression(
			DoubleCastPropExpression<?> expression) throws Exception {
		// use round if nested expression is a number
		Class<?> target = expression.exp.getTarget();
		if( Number.class.isAssignableFrom(target)){
			if( target == Double.class){
				return expression.exp.accept(this);
			}
			return new CastDoubleSQLExpression(expression.exp.accept(this));
		}
		throw new InvalidSQLPropertyException("DoublePropExpression not representable as SQLExpression");
		
	}
	public SQLExpression visitDurationCastPropExpression(
			DurationCastPropExpression<?> expression) throws Exception {
		throw new InvalidSQLPropertyException("DurationCastPropExpression not representable as SQLExpression");
		
	}
	public <T, D> SQLExpression visitTypeConverterPropExpression(
			TypeConverterPropExpression<T, D> sel) throws Exception {
		throw new InvalidSQLPropertyException("TypeConverterPropExpression not representable as SQLExpression");
		
	}


	@SuppressWarnings("unchecked")
	public final SQLExpression visitConstPropExpression(ConstPropExpression<?> constExpression) throws Exception {
		return  new ConstExpression(target,constExpression.getTarget(),constExpression.val);
	}
	
	
	public final SQLExpression visitBinaryPropExpression(BinaryPropExpression binaryPropExpression) throws Exception {
		
		SQLExpression aa = binaryPropExpression.a.accept(this);
		
		SQLExpression bb = binaryPropExpression.b.accept(this);
		
		if( ! Number.class.isAssignableFrom(aa.getTarget())){
			throw new InvalidSQLPropertyException("Non numeric SQL expression");
		}
		if( ! Number.class.isAssignableFrom(bb.getTarget())){
			throw new InvalidSQLPropertyException("Non numeric SQL expression");
		}
		
		return  BinaryExpression.create(conn,aa, binaryPropExpression.op,
					 bb);
	}
	

	@SuppressWarnings("unchecked")
	public SQLExpression visitMilliSecondDatePropExpression(
			MilliSecondDatePropExpression milliSecondDate) throws Exception {
		SQLExpression<Date> de = milliSecondDate.getDateExpression().accept(this);
		if( de instanceof DateSQLExpression){
			return ((DateSQLExpression)de).getMillis();
		}
		return sql.convertToMilliseconds(de);
		
	}

	public SQLExpression visitNamePropExpression(
			NamePropExpression namePropExpression) throws Exception {
		throw new InvalidSQLPropertyException("NamePropExpression not representable as SQLExpression");
	}
	
	public SQLExpression visitDurationSecondPropExpression(DurationSecondsPropExpression expr) throws Exception{
		PropExpression<Duration> duration = expr.getDuration();
		if( duration instanceof DurationPropExpression) {
			DurationPropExpression d = (DurationPropExpression) duration;
			return sql.dateDifference(1000L, d.start.accept(this), d.end.accept(this));
		}
		throw new InvalidSQLPropertyException("DurationSecondsPropExpression not representable as SQLExpression");
	}
	public <T extends DataObject> SQLExpression visitDoubleDeRefExpression(
			DoubleDeRefExpression<T, ?> dre) throws Exception {
		
		return visitDeRefExpression(dre);
	}
	
	public abstract <T> SQLValue<T> getSQLValue(PropExpression<T> expr) throws InvalidSQLPropertyException;
	@SuppressWarnings("unchecked")
	public <T extends DataObject> SQLExpression visitDeRefExpression(
			DeRefExpression<T, ?> dre) throws Exception {
		SQLValue a =  getSQLValue(dre.getTargetObject());
		
		if( a != null && a instanceof IndexedSQLValue ){
			IndexedSQLValue ifv = (IndexedSQLValue)a;
			ExpressionTargetFactory etf = ExpressionCast.getExpressionTargetFactory(ifv.getFactory());
			if( etf == null) {
				throw new InvalidSQLPropertyException(dre.getTargetObject());
			}
			SQLExpression remote = etf.getAccessorMap().getSQLExpression(dre.getExpression());
			if( remote instanceof DateSQLExpression){
				return new DateDerefSQLExpression<>(ifv, (DateSQLExpression)remote);
			}
			return new DerefSQLExpression(ifv, remote);
		}else{
			throw new InvalidSQLPropertyException(dre.getTargetObject());
		}
	
	}

	

	public SQLExpression visitSelectPropExpression(SelectPropExpression<?> sel)
			throws Exception {
		if( sel.length() == 1 ){
			return sel.get(0).accept(this);
		}
		if( sel.allowAny() ){
			
			for(PropExpression<?> e : sel){
				try{
					if( e != null ) {
						SQLExpression<?> expr = e.accept(this);
						if(expr != null){
							return expr;
						}
					}
				}catch(Exception e1){
					
				}
				
			}
		}
		// we might get a SQLAccessor from {@link CreateAccessorPropExpressionVisitor}
		// it its the only that that evaluates. However we can't do that here
		// because we may have more than one valid expression but only one that
		// is representabler as SQL
		throw new InvalidSQLPropertyException("Cannot access SelectPropExpression via SQL");
	}
	@SuppressWarnings("unchecked")
	public SQLExpression visitDurationPropExpression(DurationPropExpression sel)
	throws Exception {
		return sql.dateDifference(1L, sel.start.accept(this), sel.end.accept(this));
	}
	public SQLExpression<Date> convertMilliExpression(SQLExpression<? extends Number> d){
		return sql.convertToDate(d,1L);
	}	
	public <T,R> SQLExpression visitLabelPropExpression(
			LabelPropExpression<T,R> expr) throws Exception {
		throw new InvalidSQLPropertyException("LabelPropExpression not representable as SQLExpression");
	}


	public SQLExpression visitConvetMillisecondToDateExpression(
			ConvertMillisecondToDatePropExpression expr) throws Exception {
		return convertMilliExpression(expr.milli_expr.accept(this));
	}



	@SuppressWarnings("unchecked")
	@Override
	public <C extends Comparable> SQLExpression visitCompareExpression(
			ComparePropExpression<C> expr) throws Exception {
		
		return new CompareSQLExpression<C>(expr.e1.accept(this),expr.m,expr.e2.accept(this));
	}


	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor#visitConstReferenceExpression(uk.ac.ed.epcc.safe.accounting.expr.ConstReferenceExpression)
	 */
	@Override
	public <I extends Indexed> SQLExpression visitConstReferenceExpression(ConstReferenceExpression<I> expr)
			throws Exception {
		throw new InvalidSQLPropertyException("IndexedReference not representable as SQLExpression");
	}
	
	
	public SQLExpression visitLocatePropExpression(
			LocatePropExpression expr) throws Exception {
		return new LocateSQLExpression(expr.getSubstring().accept(this), expr.getString().accept(this), expr.getPosition().accept(this));
	}
	@Override
	public <T extends Comparable> SQLExpression visitArrayFuncPropExpression(ArrayFuncPropExpression<T> expr)
			throws Exception {
		SQLExpression<T> arr[] = new SQLExpression[expr.length()];
		int i=0;
		for(PropExpression<T> e: expr ){
			arr[i++]= e.accept(this);
		}
		return new ArrayFuncExpression(target,expr.getFunc(),expr.getTarget(),arr);
	}
}