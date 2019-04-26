package uk.ac.ed.epcc.safe.accounting.formatters.value;

import uk.ac.ed.epcc.safe.accounting.charts.MapperEntry;
import uk.ac.ed.epcc.safe.accounting.charts.MapperEntryInput;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
/** A {@link ValueFormatter} for {@link MapperEntry} using the format understood by {@link MapperEntryInput}
 * 
 * @see MapperEntryInput
 * @author Stephen Booth
 *
 */
public class MapperEntryFormatter extends AbstractContexed implements ValueFormatter<MapperEntry> {

	public MapperEntryFormatter(AppContext conn) {
		super(conn);
	}

	@Override
	public Class<MapperEntry> getType() {
		return MapperEntry.class;
	}

	

	@Override
	public String format(MapperEntry value) {
		String mode = value.getMode();
		if( mode == null || mode.isEmpty()) {
			return value.getName();
		}else {
			return mode+"."+value.getName();
		}
	}

	

}
