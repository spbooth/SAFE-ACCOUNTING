// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.AcceptFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterVisitor;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
@uk.ac.ed.epcc.webapp.Version("$Id: ExpressionAcceptMatchFilter.java,v 1.12 2015/03/10 16:56:01 spb Exp $")


public class ExpressionAcceptMatchFilter<T extends ExpressionTarget,I> implements AcceptFilter<T>{
	private final Class<? super T> target;
	private final MatchCondition m;

	private final PropExpression<I> expr1;
	private final PropExpression<I> expr2;
	public ExpressionAcceptMatchFilter(Class<? super T> target,PropExpression<I> expr1,MatchCondition m, PropExpression<I> expr2){
		this.target=target;
		this.expr1=expr1;
		this.m=m;
		this.expr2=expr2;
	}
	@SuppressWarnings("unchecked")
	public boolean accept(T o) {
		try {
			I res1 = o.evaluateExpression(expr1);
			I res2 = o.evaluateExpression(expr2);
			if( m == null ){
				if( res1 == null){
					return res2==null;
				}
				return res1.equals(res2);
			}
			return m.compare(res1,res2 );
		} catch (InvalidExpressionException e) {
			return false;
		}
	}
	public <X> X acceptVisitor(FilterVisitor<X, ? extends T> vis) throws Exception {
		return vis.visitAcceptFilter(this);
	}
	public Class<? super T> getTarget() {
		return target;
	}

}