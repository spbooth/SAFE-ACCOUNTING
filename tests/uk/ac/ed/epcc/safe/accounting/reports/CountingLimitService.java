package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.limits.LimitException;
import uk.ac.ed.epcc.webapp.limits.LimitService;
/** A debugging {@link LimitService} that triggers after a fixed number of
 * calls.
 * 
 * @author Stephen Booth
 *
 */
public class CountingLimitService extends LimitService {

	private int calls=0;
	private final int max_calls;
	public CountingLimitService(AppContext conn, int max_calls) {
		super(conn);
		this.max_calls=max_calls;
	}
	@Override
	public void checkLimit() throws LimitException {
		if( calls++ > max_calls) {
			throw new LimitException("Too many calls to checkLimit");
		}
		
	}

}
