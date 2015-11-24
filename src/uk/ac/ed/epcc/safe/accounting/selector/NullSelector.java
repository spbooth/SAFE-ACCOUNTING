// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.selector;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
@uk.ac.ed.epcc.webapp.Version("$Id: NullSelector.java,v 1.3 2014/09/15 14:32:29 spb Exp $")


public final class NullSelector<T> implements RecordSelector{
	public final PropExpression<T> expr;
	public final boolean is_null;
	
	public NullSelector(PropExpression<T> exp, boolean is_null){
		this.expr=exp.copy();
		this.is_null=is_null;
	}
	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		return visitor.visitNullSelector(this);
	}
	public NullSelector<T> copy() {
		return this;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + (is_null ? 1231 : 1237);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NullSelector other = (NullSelector) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (is_null != other.is_null)
			return false;
		return true;
	}

}