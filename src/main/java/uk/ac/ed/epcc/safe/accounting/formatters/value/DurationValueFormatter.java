package uk.ac.ed.epcc.safe.accounting.formatters.value;

import uk.ac.ed.epcc.webapp.content.HourTransform;
import uk.ac.ed.epcc.webapp.model.data.Duration;

/** A {@link ValueFormatter} that displays a {@link Duration} as HH:MM:SS
 * Other Numeric 
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
		return HourTransform.toHrsMinSec(object);
	}

}
