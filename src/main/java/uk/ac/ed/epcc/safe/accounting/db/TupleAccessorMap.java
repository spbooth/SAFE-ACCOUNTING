package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;

public class TupleAccessorMap extends AccessorMap {

	private final PropertyTupleFactory fac;
	protected MultiFinder finder;
	public TupleAccessorMap(PropertyTupleFactory fac,String config_tag,MultiFinder finder) {
		super(fac.getContext(), fac.getTag(), config_tag);
		this.fac=fac;
		this.finder=finder;
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
	
	
	@Override
	public BaseFilter getFilter(RecordSelector selector) throws CannotFilterException {
		return fac.addMandatoryFilter(getRawFilter(fac.mutateSelector(selector)));
	}
	/** Give access to the oroiginal getFilter implementation so that
	 * the customisation methods in the factory can
	 * 
	 * @param selector
	 * @return
	 * @throws CannotFilterException
	 */
	public BaseFilter getRawFilter(RecordSelector selector) throws CannotFilterException {
		return super.getFilter(selector);
	}
}
