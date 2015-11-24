// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
@uk.ac.ed.epcc.webapp.Version("$Id: FilterReduction.java,v 1.13 2014/09/15 14:32:20 spb Exp $")


public class FilterReduction<T extends DataObject&ExpressionTarget,R> extends FilterFinder<T, R> {
	private final AccessorMap<T> map;
	public FilterReduction(AccessorMap<T> map,ReductionTarget<R> tag) throws InvalidSQLPropertyException {
		super(map.getContext(),map.getTarget(),true);
		setMapper(new ReductionMapper<R>(map.getContext(),tag.getTarget(),tag.getReduction(),tag.getDefault(),map.getSQLExpression(tag.getExpression())));
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