package uk.ac.ed.epcc.safe.accounting.properties;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.expr.UnsupportedExpressionException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Accessor;

/** A {@link PropExpression} that encodes a function applied to an {@link ExpressionTarget}
 * 
 * These should be used sparingly as they cannot be mapped to SQL but allow
 * arbitrary java logic to be inserted into a {@link PropExpression}.
 * 
 * 
 * @author Stephen Booth
 * @see Accessor
 * @param <T> type of expression
 */
public interface MethodPropExpression<T> extends PropExpression<T> {

	/** evaluate the method on a {@link PropertyTarget}
	 * this should return null if any of the required properties are missing.
	 * 
	 * 
	 * @param target {@link PropertyTarget}
	 * @return value or null
	 * @throws Exception
	 */
	public T evaluate(PropertyTarget target) throws Exception;
	
	
	/** get a set of PropertyTags that will be required to evaluate this expression
	 * If a property in this set is not defined the expression will not evaluate.
	 * optional properties should not be included
	 * 
	 * @return Set of properties
	 */
	public Set<PropertyTag> required();
	
	
	default public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<R>)vis).visitMethodPropExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}
}
