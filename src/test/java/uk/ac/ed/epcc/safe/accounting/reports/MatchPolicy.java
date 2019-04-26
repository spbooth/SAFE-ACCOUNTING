package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterPolicy;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
/** A testing {@link FilterPolicy}
 * 
 * @author Stephen Booth
 *
 * @param <A>
 */
public class MatchPolicy<A extends DataObject> implements FilterPolicy {

	private final DataObjectFactory<A> fac;
	private final A val;
	public MatchPolicy(DataObjectFactory<A> fac,A val) {
		this.fac=fac;
		this.val=val;
	}

	@Override
	public BaseFilter getFilter() {
		return fac.getFilter(val);
	}

}
