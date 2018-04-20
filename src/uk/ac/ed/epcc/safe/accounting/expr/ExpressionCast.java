package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.ExpressionTargetFactoryComposite;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;

/** These are the static methods for converting objects to the various
 * expression interfaces. Specifically if the support is added via composites 
 * 
 * @author spb
 *
 */
public class ExpressionCast {

	/** get an {@link ExpressionTargetFactory} for a {@link DataObjectFactory}
	 * 
	 * @param fac
	 * @return {@link ExpressionTargetFactory} or null;
	 */
	public static <T extends Indexed> ExpressionTargetFactory getExpressionTargetFactory(IndexedProducer<T> fac) {
		if( fac == null || fac instanceof ExpressionTargetFactory) {
			// object implements interface directly or is null
			return (ExpressionTargetFactory) fac;
		}
		if( fac instanceof DataObjectFactory) {
			return (ExpressionTargetFactory) ((DataObjectFactory)fac).getComposite(ExpressionTargetFactoryComposite.class);
		}
		return null;
	}

	
	public static ExpressionTarget getExpressionTarget(Object o) {
		if( o == null || o instanceof ExpressionTarget) {
			return (ExpressionTarget) o;
		}
		if( o instanceof DataObject) {
			ExpressionTargetFactory etf = getExpressionTargetFactory(DataObject.getFactory((DataObject)o));
			if( etf != null) {
				return etf.getExpressionTarget(o);
			}
		}
		return null;
	}
}
