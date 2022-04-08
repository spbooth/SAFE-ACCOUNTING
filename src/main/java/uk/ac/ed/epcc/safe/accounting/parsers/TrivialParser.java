package uk.ac.ed.epcc.safe.accounting.parsers;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.AppContext;
/** A No-op parser that leaves the property map unchanged
 * 
 * @author Stephen Booth
 *
 */
public class TrivialParser extends AbstractPropertyContainerParser  {

	public TrivialParser(AppContext conn) {
		super(conn);
	}

	@Override
	public PropertyFinder initFinder(PropertyFinder prev, String table) {
		return prev;
	}

	@Override
	public boolean parse(DerivedPropertyMap map, String record) throws AccountingParseException {
		return true;
	}

	

}
