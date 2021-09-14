package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.safe.accounting.reports.AtomExtension.AtomResult;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.time.Period;

public class PiAtomPlugin extends AbstractContexed implements AtomPlugin<Number> {

	public PiAtomPlugin(AppContext conn) {
		super(conn);
	}

	@Override
	public AtomResult<Number> evaluate(Period period, RecordSet set) throws Exception {
		return new AtomResult<Number>(null,Math.PI);
	}

}
