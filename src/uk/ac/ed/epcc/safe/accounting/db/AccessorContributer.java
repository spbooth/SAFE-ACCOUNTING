package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.webapp.model.data.Composite;
import uk.ac.ed.epcc.webapp.model.data.DataObject;

/** Interdace for {@link Composite}s that add properties
 * 
 * @author spb
 *
 */
public interface AccessorContributer {
	public <P extends DataObject&ExpressionTarget> void customAccessors(AccessorMap<P> mapi2,
			MultiFinder finder, PropExpressionMap derived);
}
