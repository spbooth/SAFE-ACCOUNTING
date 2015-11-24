// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.AcceptFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterVisitor;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
@uk.ac.ed.epcc.webapp.Version("$Id: ExpressionAcceptNullFilter.java,v 1.10 2015/03/10 16:56:01 spb Exp $")


public class ExpressionAcceptNullFilter<T extends ExpressionTarget,I> implements AcceptFilter<T>{
	private final Class<? super T> target;
	private final boolean is_null;
	private final PropExpression<I> expr;
	
	public ExpressionAcceptNullFilter(Class<? super T> target,PropExpression<I> expr, boolean is_null){
		this.target=target;
		this.expr=expr;
		this.is_null=is_null;
	}

	public boolean accept(T o) {
		try {
			I res = o.evaluateExpression(expr);
			if( res == null ){
				return is_null;
			}
			if( res instanceof IndexedReference){
				return is_null == ((IndexedReference)res).isNull();
			}
			return ! is_null;
		} catch (InvalidExpressionException e) {
			return is_null;
		}
	}

	public <X> X acceptVisitor(FilterVisitor<X, ? extends T> vis) throws Exception {
		return vis.visitAcceptFilter(this);
	}

	public Class<? super T> getTarget() {
		return target;
	}


}