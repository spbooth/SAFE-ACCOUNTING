package uk.ac.ed.epcc.safe.accounting.selector;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
/** A {@link RecordSelector} that requests the results be generated in a particular order.
 * 
 * @author spb
 *
 * @param <T>
 */
public class OrderClause<T> implements RecordSelector {
	private final boolean descending;
	private final PropExpression<T> expr;
	
	public OrderClause(boolean descending,PropExpression<T> e) {
		this.descending=descending;
		this.expr=e;
	}

	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		return visitor.visitOrderClause(this);
	}

	public RecordSelector copy() {
		return this;
	}

	public PropExpression<T> getExpr() {
		return expr;
	}
	public boolean getDescending(){
		return descending;
	}

}
