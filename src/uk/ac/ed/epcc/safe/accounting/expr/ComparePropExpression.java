package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;

public class ComparePropExpression<C extends Comparable> implements PropExpression<Boolean>{

	public ComparePropExpression(PropExpression<C> e1, MatchCondition m,
			PropExpression<C> e2) {
		super();
		this.e1 = e1;
		this.m = m;
		this.e2 = e2;
	}

	public final MatchCondition m;
	public final PropExpression<C> e1;
	public final PropExpression<C> e2;
	@Override
	public Class<? super Boolean> getTarget() {
		return Boolean.class;
	}

	@Override
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<R>)vis).visitCompareExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}

	@Override
	public ComparePropExpression<C> copy() {
		return this;
	}

}
