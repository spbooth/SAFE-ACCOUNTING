package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.webapp.model.data.TupleFactory;

public class TupleAccessorMap extends AccessorMap {

	private final TupleFactory fac;
	public TupleAccessorMap(TupleFactory fac,String config_tag) {
		super(fac.getContext(), fac.getTarget(), config_tag);
		this.fac=fac;
	}
	@Override
	protected void addSource(StringBuilder sb) {
		fac.addSource(sb);
	}

	@Override
	protected String getDBTag() {
		return fac.getDBTag();
	}

}
