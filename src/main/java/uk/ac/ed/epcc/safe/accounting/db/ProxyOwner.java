package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
/** Interface for domain objects that cache an {@link ExpressionTargetContainer}
 * proxy.
 * 
 * @see ProxyOwnerContainer
 * @author Stephen Booth
 *
 */
public interface ProxyOwner {
	/** Get an {@link ExpressionTargetContainer} that is a proxy for the
	 * data in this object.
	 * 
	 * @return
	 */
	 public ExpressionTargetContainer getProxy();
}
