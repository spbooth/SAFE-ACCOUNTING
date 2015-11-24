package uk.ac.ed.epcc.safe.accounting.charts;

import java.security.Principal;

import uk.ac.ed.epcc.webapp.Indexed;
/** A {@link ReferenceLabeller} that returns the simple name rather than the full
 * identifier.
 * 
 * @author spb
 *
 * @param <D>
 */
public class ReferenceNameLabeller<D extends Indexed> extends ReferenceLabeller<D> {

	public ReferenceNameLabeller() {
		
	}

	@Override
	public String getLabel(D val) {
		if( val instanceof Principal){
			return ((Principal)val).getName();
		}
		return super.getLabel(val);
	}

	

}
