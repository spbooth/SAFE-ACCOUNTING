package uk.ac.ed.epcc.safe.accounting.policy;

import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.AppContext;
/** A version of {@link ExpressionPropertyPolicy} that adds the {@link StandardProperties} to the scope first.
 * This allows the standard properties to be defined in tables where the parser does not already add them to the scope.
 * 
 * @author spb
 *
 */
public class StandardPropertyPolicy extends ExpressionPropertyPolicy {

	public StandardPropertyPolicy() {
	}

	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table) {
		MultiFinder finder = new MultiFinder();
		finder.addFinder(prev);
		finder.addFinder(StandardProperties.base);
		finder.addFinder(StandardProperties.time);
		return super.initFinder(ctx, finder, table);
	}

}
