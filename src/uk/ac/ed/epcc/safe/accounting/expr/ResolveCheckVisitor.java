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
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.NullSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OrRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OrderClause;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.ReductionSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RelationClause;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.safe.accounting.selector.SelectorVisitor;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;

/** Visitor to check if an expression resolves
 * This version does not check expressions on remote objects
 * 
 * @author spb
 *
 */
public abstract class ResolveCheckVisitor implements PropExpressionVisitor<Boolean> {
	AppContext conn;
	Logger log;
	public ResolveCheckVisitor(AppContext conn,Logger log){
		this.conn=conn;
		this.log=log;
	}
	public ResolveCheckVisitor(){
		this.log=null;
	}
	
	public void debug(String msg){
		if( log != null ){
			log.debug(msg);
		}
	}
	public void debug( String msg,Throwable t){
		if( log != null ){
			log.debug(msg, t);
		}
	}
	public Boolean visitStringPropExpression(
			StringPropExpression<?> stringExpression) throws Exception {

		return stringExpression.exp.accept(this);
	}

	public Boolean visitIntPropExpression(IntPropExpression<?> intExpression)
			throws Exception {
		return intExpression.exp.accept(this);
	}
	public Boolean visitLongCastPropExpression(LongCastPropExpression<?> intExpression)
			throws Exception {
		return intExpression.exp.accept(this);
	}
	public Boolean visitDoubleCastPropExpression(DoubleCastPropExpression<?> doubleExpression)
	throws Exception {
		return doubleExpression.exp.accept(this);
	}
	public Boolean visitDurationCastPropExpression(DurationCastPropExpression<?> expression)
	throws Exception {
		return expression.exp.accept(this);
	}
	public Boolean visitConstPropExpression(
			ConstPropExpression<?> constExpression) throws Exception {
		return Boolean.TRUE;
	}

	public Boolean visitLocatePropExpression(
			LocatePropExpression expr) throws Exception {
		return expr.substr.accept(this) && expr.str.accept(this) && expr.pos.accept(this);
	}

	public Boolean visitBinaryPropExpression(
			BinaryPropExpression binaryPropExpression) throws Exception {
	
		return Boolean.valueOf( binaryPropExpression.a.accept(this) && binaryPropExpression.b.accept(this));
	}

	public Boolean visitMilliSecondDatePropExpression(
			MilliSecondDatePropExpression milliSecondDate) throws Exception {
		
		return milliSecondDate.getDateExpression().accept(this);
	}

	public Boolean visitNamePropExpression(NamePropExpression namePropExpression)
			throws Exception {
		return namePropExpression.getTargetRef().accept(this);
	}
	public <T extends DataObject & ExpressionTarget> Boolean visitDoubleDeRefExpression(
			DoubleDeRefExpression<T, ?> deRefExpression) throws Exception {
		return visitDeRefExpression(deRefExpression);
	}
	public <T extends DataObject & ExpressionTarget> Boolean visitDeRefExpression(
			DeRefExpression<T, ?> deRefExpression) throws Exception {
		ReferenceExpression<T> ref = deRefExpression.getTargetObject();
		Boolean accept = ref.accept(this);
		if( accept.booleanValue()){
			if( conn != null ){
				// ok we now the reference property exists can we check the remote
				// expression we need an AppContext to do this
				@SuppressWarnings("unchecked")
				ExpressionTargetFactory<T> fac  = (ExpressionTargetFactory<T>) ref.getFactory(conn);
				return fac.getAccessorMap().resolves(deRefExpression.getExpression(), false);
			}
		}
		return accept;
	}

	
	

	public Boolean visitSelectPropExpression(SelectPropExpression<?> sel)
			throws Exception {
		for( PropExpression<?> e : sel){
			
		   try{
			if( e.accept(this)){
				return true;
			}
		   }catch(Exception ee){
			   
		   }
		}
		return Boolean.FALSE;
	}

	public Boolean visitDurationPropExpression(DurationPropExpression sel)
			throws Exception {
		return Boolean.valueOf(sel.start.accept(this) && sel.end.accept(this));
	}
	public Boolean visitDurationSecondPropExpression(DurationSecondsPropExpression sel)
			throws Exception {
		return sel.getDuration().accept(this);
	}
	public <T, D> Boolean visitTypeConverterPropExpression(
			TypeConverterPropExpression<T, D> sel) throws Exception {
		return sel.getInnerExpression().accept(this);
	}
	public <T,R> Boolean visitLabelPropExpression(LabelPropExpression<T,R> expr)
			throws Exception {
		//TODO what if labeller has default value, do we care ?
		return expr.getExpr().accept(this);
	}
	/** Check if a {@link RecordSelector} is compatible with a {@link CasePropExpression}.
	 * 
	 * @author spb
	 *
	 */
	public class CaseCompatibleVisitor implements SelectorVisitor<Boolean>{

		public Boolean visitAndRecordSelector(AndRecordSelector a) throws Exception {
			for(RecordSelector s: a){
				if( ! s.visit(this)){
					return false;
				}
			}
			return true;
		}

		public Boolean visitOrRecordSelector(OrRecordSelector o) throws Exception {
			for(RecordSelector s: o){
				if( s.visit(this)){
					return true;
				}
			}
			return false;
		}

		public <I> Boolean visitClause(SelectClause<I> c) throws Exception {
			return c.tag.accept(ResolveCheckVisitor.this);
		}

		public <I> Boolean visitNullSelector(NullSelector<I> n)
				throws Exception {
			return n.expr.accept(ResolveCheckVisitor.this);
		}

		public <I> Boolean visitRelationClause(RelationClause<I> c)
				throws Exception {
			return c.left.accept(ResolveCheckVisitor.this) && c.right.accept(ResolveCheckVisitor.this);
		}

		public Boolean visitPeriodOverlapRecordSelector(
				PeriodOverlapRecordSelector o) throws Exception {
			return o.getStart().accept(ResolveCheckVisitor.this) && o.getEnd().accept(ResolveCheckVisitor.this);
		}

		public <I> Boolean visitOrderClause(OrderClause<I> o) throws Exception {
			throw new CannotFilterException("Cannot use OrderClause in CaseExpression");
		}

		public Boolean visitReductionSelector(ReductionSelector r)
				throws Exception {
			throw new CannotFilterException("Cannot use ReductionSelector in CaseExpression");
		}
		
	}
	
	public <T> Boolean visitCasePropExpression(CasePropExpression<T> expr)
			throws Exception {
		CaseCompatibleVisitor vis = new CaseCompatibleVisitor();
		for(CasePropExpression.Case<T> c : expr.getCases()){
			if( ! c.sel.visit(vis)){
				return false;
			}
			if( ! c.expr.accept(this)){
				return false;
			}
		}
		PropExpression<? extends T> def = expr.getDefaultExpression();
		if( def == null ){
			return Boolean.TRUE;
		}
		return def.accept(this);
	}
	public Boolean visitConvetMillisecondToDateExpression(
			ConvertMillisecondToDatePropExpression expr) throws Exception {
		return expr.milli_expr.accept(this);
	}
	
	@Override
	public <C extends Comparable> Boolean visitCompareExpression(
			ComparePropExpression<C> expr) throws Exception {
		return Boolean.valueOf( expr.e1.accept(this) && expr.e2.accept(this));
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor#visitConstReferenceExpression(uk.ac.ed.epcc.safe.accounting.expr.ConstReferenceExpression)
	 */
	@Override
	public <I extends Indexed> Boolean visitConstReferenceExpression(ConstReferenceExpression<I> expr)
			throws Exception {
		return Boolean.TRUE;
	}

}