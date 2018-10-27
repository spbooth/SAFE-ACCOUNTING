package uk.ac.ed.epcc.safe.accounting.formatters.value;

import uk.ac.ed.epcc.safe.accounting.charts.ChartService;
import uk.ac.ed.epcc.safe.accounting.charts.PlotEntry;
import uk.ac.ed.epcc.safe.accounting.charts.PlotEntryInput;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
/** A {@link ValueFormatter} for {@link PlotEntry} using the format understood by {@link PlotEntryInput}
 * 
 * @see PlotEntryInput
 * @author Stephen Booth
 *
 */
public class PlotEntryFormatter extends AbstractContexed implements ValueFormatter<PlotEntry> {

	public PlotEntryFormatter(AppContext conn) {
		super(conn);
	}

	@Override
	public Class<PlotEntry> getType() {
		return PlotEntry.class;
	}

	

	@Override
	public String format(PlotEntry value) {
		String mode = value.getMode();
		if( mode == null || mode.isEmpty()) {
			return value.getName();
		}else {
			return mode+"."+value.getName();
		}
	}

	

}
