package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.ExpressionTargetFactoryComposite;
import uk.ac.ed.epcc.safe.accounting.properties.OwnedPropertyTarget;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.selector.PropertyTargetGenerator;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.Composable;
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
	 * @throws InvalidArgument 
	 */
	public static <T extends Indexed> ExpressionTargetFactory<T> getExpressionTargetFactory(IndexedProducer<T> fac)  {
	    // Don't use Composable static method as we want to return null
		if( fac == null || fac instanceof ExpressionTargetFactory) {
			// object implements interface directly or is null
			return (ExpressionTargetFactory<T>) fac;
		}
		if( fac instanceof DataObjectFactory) {
			return (ExpressionTargetFactory<T>) ((DataObjectFactory)fac).getComposite(ExpressionTargetFactoryComposite.class);
		}
		return null;
	}

	/** get an {@link PropertyTargetGenerator} for a {@link DataObjectFactory}
	 * 
	 * @param fac
	 * @return {@link PropertyTargetGenerator} or null;
	 * @throws InvalidArgument 
	 */
	public static <T extends Indexed> PropertyTargetGenerator<T> getPropertyTargetGenerator(IndexedProducer<T> fac) throws InvalidArgument {
		return Composable.getComposable(PropertyTargetGenerator.class,ExpressionTargetFactoryComposite.class,fac);
		
	}
	/** construct an {@link ExpressionTargetFactory} from a tag.
	 * 
	 * @param conn
	 * @param tag
	 * @return
	 */
	public static <T extends Indexed> ExpressionTargetFactory makeExpressionTargetFactory(AppContext conn,String tag) {
		return getExpressionTargetFactory(conn.makeObject(DataObjectFactory.class, tag));
	}
	/** construct an {@link ExpressionTargetFactory} from a tag.
	 * 
	 * @param conn
	 * @param tag
	 * @return
	 */
	public static <T extends Indexed> PropertyTargetGenerator<T> makePropertyTargetGenerator(AppContext conn,String tag) {
		try {
			return getPropertyTargetGenerator(conn.makeObject(DataObjectFactory.class, tag));
		} catch (InvalidArgument e) {
			conn.getService(LoggerService.class).getLogger(ExpressionCast.class).error("Unexpected error", e);
			return null;
		}
	}
	public static ExpressionTarget getExpressionTarget(AppContext ctx,Object o) {
		if( o == null || o instanceof ExpressionTarget) {
			return (ExpressionTarget) o;
		}
		if( o instanceof DataObject) {
			ExpressionTargetFactory etf = getExpressionTargetFactory(DataObject.getOwningFactory((DataObject)o));
			if( etf != null) {
				return etf.getExpressionTarget(o);
			}
		}
		if( o instanceof OwnedPropertyTarget) {
			return new OwnedPropertyTargetExpressionWrapper(ctx, (OwnedPropertyTarget) o);
		}
		return null;
	}
	public static PropertyContainer getPropertyContainer(Object o) {
		if( o == null || o instanceof PropertyContainer) {
			return (PropertyContainer) o;
		}
		if( o instanceof DataObject) {
			ExpressionTargetFactory etf = getExpressionTargetFactory(DataObject.getOwningFactory((DataObject)o));
			if( etf != null) {
				return etf.getExpressionTarget(o);
			}
		}
		return null;
	}
}
