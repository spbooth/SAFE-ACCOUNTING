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
package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.safe.accounting.reference.IndexedTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** A PropExpressionVisitor that evaluates an expression.
 * 
 
 * @author spb
 *
 */
public abstract class EvaluatePropExpressionVisitor implements
		PropExpressionVisitor<Object>, Contexed {
	

	private final AppContext conn;

	public EvaluatePropExpressionVisitor(AppContext ctx) {
		conn = ctx;
	}

	public AppContext getContext() {
		return conn;
	}

	public Number visitBinaryPropExpression(BinaryPropExpression bexpr)
			throws Exception {
		Number a = (Number) bexpr.a.accept(this);
		if (a == null)
			return null;
		Number b = (Number) bexpr.b.accept(this);
		if (b == null)
			return null;
//		Logger log = conn.getService(LoggerService.class).getLogger(getClass());
//		log.debug("Evaluate Operate "+bexpr.op.toString()+" "+
//		(a==null?"null":a.getClass().getName()+":"+a.toString())+
//		" "+
//		(b==null?"null":b.getClass().getName()+":"+b.toString()));
		return bexpr.op.operate(a, b);
	}

	public Object visitConstPropExpression(
			ConstPropExpression<?> constExpression) throws Exception {
		return constExpression.val;
	}

	

	@SuppressWarnings("unchecked")
	public <T extends DataObject & ExpressionTarget> Object visitDeRefExpression(
			DeRefExpression<T, ?> deRefExpression) throws Exception {
		IndexedReference ref = (IndexedReference) deRefExpression
				.getTargetObject().accept(this);
		if (ref == null || ref.isNull()) {
			if( deRefExpression.getTarget() == IndexedReference.class){
				// return a null IndexedReference if we can
				// This allows formatters to mark unknown values without marking
				// a real null which might be present in a category total of a table
				PropExpression e = deRefExpression;
				while( e instanceof DeRefExpression){
					e = ((DeRefExpression<?,T>)e).getExpression();
				}
				if(e != null && e instanceof IndexedTag){
					return  ((IndexedTag)e).makeReference(null);
				}
			}
			return null;
		}
		Indexed target = ref.getIndexed(getContext());
		PropExpression<?> expression = deRefExpression.getExpression();
		if (target instanceof ExpressionTarget) {

			return ((ExpressionTarget) target).evaluateExpression(expression);
		} else if (target instanceof PropertyTarget) {
			PropertyTargetEvaluatePropExpressionVisitor vis = new PropertyTargetEvaluatePropExpressionVisitor(getContext(), (PropertyTarget) target);
			return expression.accept(vis);
		}
		throw new PropertyCastException(
				"Target of DeRefExpresion not an ExpressionTarget");
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor#visitdoubleDeRefExpression(uk.ac.ed.epcc.safe.accounting.expr.DoubleDeRefExpression)
	 */
	@Override
	public <T extends DataObject & ExpressionTarget> Object visitDoubleDeRefExpression(
			DoubleDeRefExpression<T, ?> deRefExpression) throws Exception {
		// No special handling
		return visitDeRefExpression(deRefExpression);
	}

	public Duration visitDurationPropExpression(DurationPropExpression sel)
			throws Exception {

		return new Duration((Date) sel.start.accept(this),
				(Date) sel.end.accept(this));
	}

	public Long visitMilliSecondDatePropExpression(
			MilliSecondDatePropExpression milliSecondDate) throws Exception {
		Date d = (Date) milliSecondDate.getDateExpression().accept(this);
		if (d == null)
			return null;
		return Long.valueOf(d.getTime());
	}

	@SuppressWarnings("unchecked")
	public String visitNamePropExpression(NamePropExpression namePropExpression)
			throws Exception {
		IndexedReference ref = (IndexedReference) namePropExpression
				.getTargetRef().accept(this);
		return NamePropExpression.refToName(getContext(), ref);
	}

	public Object visitSelectPropExpression(SelectPropExpression<?> sel)
			throws Exception {
		for (PropExpression<?> e : sel) {
			try{
			Object o = e.accept(this);
			if (o != null) {
				if( o instanceof IndexedReference){
					if( ! ((IndexedReference)o).isNull()){
						return o;
					}
				}else{
					return o;
				}
			}
			}catch(InvalidPropertyException ee){
				
			}
		}
		return null;
	}

	public String visitStringPropExpression(
			StringPropExpression<?> stringExpression) throws Exception {
		return stringExpression.exp.accept(this).toString();
	}
	public Integer visitIntPropExpression(
			IntPropExpression<?> stringExpression) throws Exception {
		Object temp = stringExpression.exp.accept(this);
		if( temp != null ){
	    	if( temp instanceof Number ){
	    		return Integer.valueOf(((Number)temp).intValue());
	    	}
	    	if( temp instanceof String){
	    		return Integer.parseInt((String)temp);
	    	}
	    }
		return null;
	}
	public Long visitLongCastPropExpression(
			LongCastPropExpression<?> stringExpression) throws Exception {
		Object temp = stringExpression.exp.accept(this);
		if( temp != null ){
	    	if( temp instanceof Number ){
	    		return Long.valueOf(((Number)temp).longValue());
	    	}
	    	if( temp instanceof String){
	    		return Long.parseLong((String)temp);
	    	}
	    }
		return null;
	}
	public Double visitDoubleCastPropExpression(
			DoubleCastPropExpression<?> stringExpression) throws Exception {
		Object temp = stringExpression.exp.accept(this);
		if( temp != null ){
	    	if( temp instanceof Number ){
	    		return Double.valueOf(((Number)temp).doubleValue());
	    	}
	    	if( temp instanceof String){
	    		return Double.parseDouble((String)temp);
	    	}
	    }
		return null;
	}
	public Duration visitDurationCastPropExpression(
			DurationCastPropExpression<?> expression) throws Exception {
		Object temp = expression.exp.accept(this);
		if( temp != null ){
			if( temp instanceof Duration){
				return (Duration) temp;
			}
	    	if( temp instanceof Number ){
	    		return new Duration((Number)temp,expression.resolution);
	    	}
	    }
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T,D> T visitTypeConverterPropExpression(
			TypeConverterPropExpression<T, D> sel) throws Exception {
		
		return sel.getConverter().find((D)sel.getInnerExpression().accept(this));
	}

	@SuppressWarnings("unchecked")
	public <T,R> R visitLabelPropExpression(LabelPropExpression<T,R> expr) throws Exception {
		return expr.getLabeller().getLabel(conn, (T) expr.getExpr().accept(this));
	}

	public Object visitDurationSecondPropExpression(
			DurationSecondsPropExpression d) throws Exception {
		Duration dur = (Duration) d.getDuration().accept(this);
		if( dur == null ){
			return null;
		}
		return dur.getSeconds();
	}
	
	public Object visitLocatePropExpression(
			LocatePropExpression l) throws Exception {
		String col = (String) l.getColumn().accept(this);
		String str = (String) l.getString().accept(this);
		Integer pos = (Integer) l.getPosition().accept(this);
		
		return col.indexOf(str, pos.intValue()-1);
	}

	/** Check if a {@link RecordSelector} matches the target object
	 * 
	 * @param sel
	 * @return
	 */
	protected abstract boolean matches(RecordSelector sel) throws Exception;
	
	public <T> Object visitCasePropExpression(CasePropExpression<T> expr)
			throws Exception {
		for(CasePropExpression.Case<T> c: expr.getCases()){
			if(matches(c.sel)){
				c.expr.accept(this);
			}
		}
		PropExpression<? extends T> def = expr.getDefaultExpression();
		if( def == null ){
			return null;
		}
		return def.accept(this);
	}
	public Object visitConvetMillisecondToDateExpression(
			ConvertMillisecondToDatePropExpression expr) throws Exception {
		return new Date(((Number)expr.milli_expr.accept(this)).longValue());
	}

	@Override
	public <C extends Comparable> Object visitCompareExpression(
			ComparePropExpression<C> expr) throws Exception {
		if( expr.m == null){
			return expr.e1.accept(this).equals(expr.e2.accept(this));
		}
		return expr.m.compare(expr.e1.accept(this), expr.e2.accept(this));
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor#visitConstReferenceExpression(uk.ac.ed.epcc.safe.accounting.expr.ConstReferenceExpression)
	 */
	@Override
	public <I extends Indexed> Object visitConstReferenceExpression(ConstReferenceExpression<I> expr) throws Exception {
		return expr;
	}
}