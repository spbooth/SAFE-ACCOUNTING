package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
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
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.time.Period;
/** A {@link SelectVisitor} that tests if a {@link RecordSelector}
 * matches an {@link ExpressionTarget}.
 * 
 * @author spb
 *
 */
public class MatchSelectVisitor<T extends ExpressionTarget> implements SelectorVisitor<Boolean> {

	private T target;
	
	public MatchSelectVisitor(T target) {
		this.target=target;
	}

	public Boolean visitAndRecordSelector(AndRecordSelector a) throws Exception {
		for( RecordSelector sel : a){
			Boolean ans = sel.visit(this);
			if( ans != null && ! ans){
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	public Boolean visitOrRecordSelector(OrRecordSelector o) throws Exception {
		for( RecordSelector sel : o){
			Boolean ans = sel.visit(this);
			if( ans != null && ans){
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public <I> Boolean visitClause(SelectClause<I> c) throws Exception {
		if( c.tag == null ){
			return Boolean.FALSE;
		}
		I val = target.evaluateExpression( c.tag, null);
		if( val == null ){
			return Boolean.FALSE;
		}
		if( c.match == null ){
			return val.equals(c.data);
		}
		
		return c.match.compare( val, c.data);
		
	}

	public <I> Boolean visitNullSelector(NullSelector<I> n) throws Exception {
		I val = target.evaluateExpression(n.expr, null);
		if( n.is_null){
			return val == null;
		}else{
			return val != null;
		}
	}

	public <I> Boolean visitRelationClause(RelationClause<I> c)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Boolean visitPeriodOverlapRecordSelector(
			PeriodOverlapRecordSelector o) throws Exception {
		Date start = target.evaluateExpression(o.getStart(), null);
		Date end = target.evaluateExpression(o.getEnd());
		
		if( start == null){
			Period p = o.getPeriod();
			return p.contains(end);
		}
		
		// TODO Auto-generated method stub
		return o.getType().overlaps(new Period(start,end), o.getPeriod());
	}

	public <I> Boolean visitOrderClause(OrderClause<I> o) throws Exception {
		// No impact on result
		return null;
	}

	public Boolean visitReductionSelector(ReductionSelector r) throws Exception {
		// Ignore these
		return null;
	}

}
