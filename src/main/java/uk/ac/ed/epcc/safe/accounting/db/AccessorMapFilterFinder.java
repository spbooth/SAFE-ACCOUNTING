package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

import uk.ac.ed.epcc.webapp.jdbc.filter.FilterFinder;
import uk.ac.ed.epcc.webapp.model.data.Repository;

public class AccessorMapFilterFinder<T,R> extends FilterFinder<T, R> {
	private final AccessorMap<T> map;
	public AccessorMapFilterFinder(AccessorMap<T> map) {
		super(map.getContext(),map.getFilterTag(),true);
		this.map=map;
	}
	@Override
	protected void addSource(StringBuilder sb) {
		map.addSource(sb);
		
	}
	@Override
	protected String getDBTag() {
		return map.getDBTag();
	}
	@Override
	protected Set<Repository> getSourceTables() {
		return map.getSourceTables();
	}
}
