package uk.ac.ed.epcc.safe.accounting.formatters.value;

import uk.ac.ed.epcc.webapp.content.HourTransform;
import uk.ac.ed.epcc.webapp.model.data.Duration;

/** A {@link ValueFormatter} that displays a {@link Duration} 
 * as HH:MM:SS
 * Other Numeric types are assumed to be in milliseconds. 
 * 
 * @author Stephen Booth
 *
 */
public class DurationValueFormatter implements ValueFormatter<Number> {

	public DurationValueFormatter() {
	}

	@Override
	public Class<Number> getType() {
		return Number.class;
	}

	@Override
	public String format(Number object) {
		// An actual Duration type would be formatted correctly but
		// its numeric value is actually in milliseconds
		// 
		// Note a non-exact division of a Duration may generate a Double
		// with resolution of milliseconds 
		// so always format other numeric types as well.
		return HourTransform.toHrsMinSec(((Number) object).longValue()/1000L);
	}

}
