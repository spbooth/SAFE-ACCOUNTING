// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.IllegalReductionException;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository;
@uk.ac.ed.epcc.webapp.Version("$Id: IndexReductionFinder.java,v 1.14 2014/09/15 14:32:20 spb Exp $")


public class IndexReductionFinder<T extends DataObject&ExpressionTarget> extends FilterFinder<T,Map<ExpressionTuple,ReductionMapResult>>{
	private final AccessorMap<T> map;
	public IndexReductionFinder(AccessorMap<T> map,Set<ReductionTarget> sum,ReductionMapResult defs) throws InvalidPropertyException, IllegalReductionException, CannotUseSQLException {
		super(map.getContext(),map.getTarget(),true);
		setMapper(new IndexReductionMapper<T>(map, sum,defs));
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
}