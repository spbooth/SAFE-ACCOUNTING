package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.content.Labeller;
/** A PropExpression that maps a nested expression to a String
 * using a {@link Labeller}
 * 
 * @author spb
 *
 * @param <T> Type of nested expression
 * @param <R> Return type of labeller
 */
public class LabelPropExpression<T,R> implements PropExpression<R> {

	private final Labeller<T,R> labeller;
	private final PropExpression<T> expr;
	

	public LabelPropExpression(Labeller<T,R> labeller, PropExpression<T> expr) {
		super();
		this.labeller = labeller;
		this.expr = expr;
	}

	public Class<? super R> getTarget() {
		return labeller.getTarget();
	}

	public <X> X accept(BasePropExpressionVisitor<X> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<X>)vis).visitLabelPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
		
	}

	public PropExpression<R> copy() {
		return this;
	}

	public Labeller<T,R> getLabeller() {
		return labeller;
	}

	public PropExpression<T> getExpr() {
		return expr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result
				+ ((labeller == null) ? 0 : labeller.hashCode());
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
		LabelPropExpression other = (LabelPropExpression) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (labeller == null) {
			if (other.labeller != null)
				return false;
		} else if (!labeller.equals(other.labeller))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Label("+labeller.getClass().getSimpleName()+","+expr.toString()+")";
	}

}
