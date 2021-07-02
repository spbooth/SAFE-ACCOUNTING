package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.LinkedHashSet;
import java.util.Set;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.EnumeratingLabeller;
/** An {@link EnumeratingLabeller} for {@link Number}s that generates
 * all positive values (including zero) up to the largest value seen by the labeller.
 * 
 * 
 * This is intended for generating histograms (as a bar chart) from binned values. The group value should
 * be scaled so the bin-width maps to a unit width. An {@link EnumeratingLabeller} is used to
 * ensure un-populated bins are present in the bar-chart.
 * 
 * @author Stephen Booth
 *
 */
public class IntegerEnumeratingLabeller implements EnumeratingLabeller<Number, Integer> {

	private int max=0;
	public IntegerEnumeratingLabeller() {
	}
	@Override
	public Integer getLabel(AppContext conn, Number key) {
		int i = key.intValue();
		if( i > max) {
			max = i;
		}
		return Integer.valueOf(i);
	}
	@Override
	public boolean accepts(Object o) {
		return o instanceof Number;
	}
	@Override
	public Class<Integer> getTarget() {
		return Integer.class;
	}
	@Override
	public Set<Integer> getRange() {
		LinkedHashSet<Integer> result = new LinkedHashSet<Integer>();
		for(int i=0 ; i <= max ; i++) {
			result.add(Integer.valueOf(i));
		}
		return result;
	}

}
