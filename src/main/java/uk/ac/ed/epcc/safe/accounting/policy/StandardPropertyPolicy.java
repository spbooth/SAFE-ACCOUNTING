package uk.ac.ed.epcc.safe.accounting.policy;

import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** A version of {@link ExpressionPropertyPolicy} that adds the {@link StandardProperties} to the scope first.
 * This allows the standard properties to be defined in tables where the parser does not already add them to the scope.
 * 
 * @author spb
 *
 */
public class StandardPropertyPolicy extends ExpressionPropertyPolicy {

	public StandardPropertyPolicy(AppContext conn) {
		super(conn);
	}

	@Override
	public PropertyFinder initFinder(PropertyFinder prev, String table) {
		MultiFinder finder = new MultiFinder();
		finder.addFinder(StandardProperties.base);
		finder.addFinder(StandardProperties.time);
		finder.addFinder(super.initFinder(prev, table));
		return finder;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.ExpressionPropertyPolicy#getTableTransitionSummary(uk.ac.ed.epcc.webapp.content.ContentBuilder, uk.ac.ed.epcc.webapp.session.SessionService)
	 */
	@Override
	public void getTableTransitionSummary(ContentBuilder hb, SessionService operator) {
		super.getTableTransitionSummary(hb, operator);
		hb.addText("A set of standard properties are automatically added by this policy so definitions can be defined for them.");
	}

}
