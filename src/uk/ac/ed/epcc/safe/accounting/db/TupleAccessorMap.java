package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
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
	@Override
	public BaseFilter getRelationshipFilter(String relationship) throws CannotFilterException {
		throw new CannotFilterException("No relationship filters on Tuples");
	}
	@Override
	protected Set getSourceTables() {
		return fac.getSourceTables();
	}

}
