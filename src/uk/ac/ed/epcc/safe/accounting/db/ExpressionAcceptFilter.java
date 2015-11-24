// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.AcceptFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterVisitor;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
@uk.ac.ed.epcc.webapp.Version("$Id: ExpressionAcceptFilter.java,v 1.14 2015/03/10 16:56:02 spb Exp $")

/** An {@link AcceptFilter} based on evaluating expression as run-time.
 * 
 * @author spb
 *
 * @param <T> type filter is for
 * @param <I> data type
 */
public class ExpressionAcceptFilter<T extends ExpressionTarget,I> implements AcceptFilter<T>{
	private final Class<? super T> target;
	private final MatchCondition m;
	private final I data;
	private final PropExpression<I> expr;
	public ExpressionAcceptFilter(Class<? super T> target,PropExpression<I> expr,MatchCondition m, I data){
		this.target=target;
		this.expr=expr;
		this.m=m;
		this.data=data;
		assert(data!=null);
	}
	@SuppressWarnings("unchecked")
	public boolean accept(T o) {
		try {
			I res = o.evaluateExpression(expr);
			if( m == null ){
				return data.equals(res);
			}
			boolean compare = m.compare(res, data);
			return compare;
		} catch (InvalidExpressionException e) {
			return false;
		}
	}
	public <X> X acceptVisitor(FilterVisitor<X, ? extends T> vis) throws Exception {
		return vis.visitAcceptFilter(this);
	}
	@Override
	public String toString() {
		return "ExpressionAcceptFilter ["+expr+" "+ (m == null ? "=" : m.toString())+" "+data+"]";
	}
	public Class<? super T> getTarget() {
		return target;
	}

}