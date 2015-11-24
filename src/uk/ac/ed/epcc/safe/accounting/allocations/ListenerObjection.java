package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.webapp.forms.exceptions.ValidateException;

/** {@link Exception} thrown when a {@link AllocationListener}
 * wishes to veto a proposed operation
 * 
 * @author spb
 *
 */
public class ListenerObjection extends ValidateException {

	public ListenerObjection() {
		
	}

	public ListenerObjection(String message) {
		super(message);
	}


}
