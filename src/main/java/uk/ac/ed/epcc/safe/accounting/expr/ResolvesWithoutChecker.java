package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.AccessorMap.ResolveChecker;
import uk.ac.ed.epcc.safe.accounting.properties.MethodPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;

/** Check that a {@link PropExpression} resolves without using any
 * of a forbidden set of {@link PropertyTag}s
 * 
 * @author Stephen Booth
 *
 */
public class ResolvesWithoutChecker extends ResolveCheckVisitor {

	private final Set<PropertyTag> forbidden;
	/**
	 * @param conn
	 * @param log
	 */
	public ResolvesWithoutChecker(AppContext conn, Logger log, Set<PropertyTag> forbidden) {
		super(conn, log);
		this.forbidden=forbidden;
	}

	@Override
	public Boolean visitPropertyTag(PropertyTag<?> tag) throws Exception {
		return ! forbidden.contains(tag);
	}

	@Override
	public Boolean visitMethodPropExpression(MethodPropExpression<?> method) throws Exception {
		for( PropertyTag t : method.required()) {
			if( forbidden.contains(t)) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	
}
