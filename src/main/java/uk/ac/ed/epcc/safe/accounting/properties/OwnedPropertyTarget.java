package uk.ac.ed.epcc.safe.accounting.properties;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.OwnedPropertyTargetExpressionWrapper;

/** A {@link PropertyTarget} which comes from an owning {@link PropertyTargetFactory}
 * and can generate this.
 * 
 * These can be automatically converted into an {@link ExpressionTarget} by wrapping in a
 * {@link OwnedPropertyTargetExpressionWrapper}.
 * 
 * @author Stephen Booth
 *
 */
public interface OwnedPropertyTarget extends PropertyTarget {

	public PropertyTargetFactory getPropertyTargetFactory();
}
