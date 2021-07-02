package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.LinkedHashSet;
import java.util.Set;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.EnumeratingLabeller;

/** An {@link EnumeratingLabeller} variant of {@link Power2RangeLabeller}
 * 
 * @author Stephen Booth
 *
 * @param <T>
 */
public class Power2RangeEnumeratingLabeller<T extends Number> extends Power2RangeLabeller<T> implements EnumeratingLabeller<T, Power2RangeLabeller.Range>{

	private Range min=null;
	private Range max=null;
	@Override
	public Set<Range> getRange() {
		Set<Range> result = new LinkedHashSet<Power2RangeLabeller.Range>();
		if( min != null && max != null ) {
			for(Range r=min; r.compareTo(max) <= 0 ; r = getRange(r.getUpper()+1)) {
				result.add(r);
			}
		}
		return result;
	}
	@Override
	public Range getLabel(AppContext conn, T key) {
		Range l = super.getLabel(conn, key);
		
		if( min == null || l.compareTo(min) < 0) {
			min = l;
		}
		if( max == null || l.compareTo(max)  > 0) {
			max=l;
		}
		
		return l;
	}
}
