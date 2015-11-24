// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Date;
import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.CasePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ComparePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConvertMillisecondToDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DoubleCastPropExpression;
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
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.BinaryExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.CastDoubleSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.CastLongSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.CompareSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.ConstExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.DerefSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.RoundSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.StringConvertSQLExpression;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.IndexedFieldValue;
import uk.ac.ed.epcc.webapp.model.data.expr.DurationSQLExpression;
/** get an SQLExpression from a PropExpression
 * 
 * @author spb
 *
 */
public abstract class CreateSQLExpressionPropExpressionVisitor implements
		PropExpressionVisitor<SQLExpression> {
	
	private AppContext conn;
	
	public CreateSQLExpressionPropExpressionVisitor(AppContext c){
		this.conn=c;
	}
    
	
	@SuppressWarnings("unchecked")
	public SQLExpression visitStringPropExpression(
			StringPropExpression<?> stringExpression) throws Exception {
		if( Number.class.isAssignableFrom(stringExpression.exp.getTarget())){
			// SQL can treat numbers as strings
			return new StringConvertSQLExpression(stringExpression.exp.accept(this));
		}
		throw new InvalidSQLPropertyException("StringPropExpression not representable as SQLExpression");
		
	}

	public SQLExpression visitIntPropExpression(
			IntPropExpression<?> intExpression) throws Exception {
		// use round if nested expression is a number
		if( Number.class.isAssignableFrom(intExpression.exp.getTarget())){
			return new RoundSQLExpression(intExpression.exp.accept(this));
		}
		throw new InvalidSQLPropertyException("IntPropExpression not representable as SQLExpression");
		
	}
	public SQLExpression visitLongCastPropExpression(
			LongCastPropExpression<?> intExpression) throws Exception {
		// use round if nested expression is a number
		if( Number.class.isAssignableFrom(intExpression.exp.getTarget())){
			return new CastLongSQLExpression<Number>(intExpression.exp.accept(this));
		}
		throw new InvalidSQLPropertyException("LongPropExpression not representable as SQLExpression");
		
	}
	public SQLExpression visitDoubleCastPropExpression(
			DoubleCastPropExpression<?> expression) throws Exception {
		// use round if nested expression is a number
		if( Number.class.isAssignableFrom(expression.exp.getTarget())){
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
		return  new ConstExpression(constExpression.getTarget(),constExpression.val);
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
		return convertDateExpression(de);
		
	}

	public SQLExpression visitNamePropExpression(
			NamePropExpression namePropExpression) throws Exception {
		throw new InvalidSQLPropertyException("NamePropExpression not representable as SQLExpression");
	}
	
	public SQLExpression visitDurationSecondPropExpression(DurationSecondsPropExpression expr) throws Exception{
		throw new InvalidSQLPropertyException("DurationSecondsPropExpression not representable as SQLExpression");
	}

	@SuppressWarnings("unchecked")
	public <T extends DataObject & ExpressionTarget> SQLExpression visitDeRefExpression(
			DeRefExpression<T, ?> dre) throws Exception {
		SQLValue a =  dre.getTargetObject().accept(this);
		
		if( a != null && a instanceof IndexedFieldValue ){
			IndexedFieldValue ifv = (IndexedFieldValue)a;
			SQLExpression remote = ((ExpressionTargetFactory)ifv.getFactory()).getAccessorMap().getSQLExpression(dre.getExpression());
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
			Iterator<?> it = sel.iterator();
			while(it.hasNext()){
				PropExpression<?> e = (PropExpression<?>) it.next();
				try{
					SQLExpression<?> expr = e.accept(this);
					if(expr != null){
						return expr;
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
		return new DurationSQLExpression(convertDateExpression(sel.start.accept(this)), convertDateExpression(sel.end.accept(this)));
	}
	/** Method to convert a Date expression to milliseconds.
	 * 
	 * @param d
	 * @return SQLExpression
	 */
	public abstract SQLExpression<? extends Number> convertDateExpression(SQLExpression<Date> d);

	/** Method to convert a millisecond expression to Date.
	 * 
	 * @param d
	 * @return SQLExpression
	 */
	public abstract SQLExpression<Date> convertMilliExpression(SQLExpression<? extends Number> d);

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
	
	

}