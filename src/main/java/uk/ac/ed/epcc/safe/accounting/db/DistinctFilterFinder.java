package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.CountReduction;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.webapp.DistinctCount;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.expr.DistinctMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterFinder;
/** A {@link FilterFinder} that generates a {@link DistinctCount} value
 * from the distinct values of the reduction
 * 
 * this will work with any {@link ReductionTarget} using DISTICT reduction
 * but normally this will be a {@link CountReduction} 
 * 
 * @author Stephen Booth
 *
 * @param <T>
 */
public class DistinctFilterFinder<T> extends AccessorMapFilterFinder<T, DistinctCount> {

	public DistinctFilterFinder(AccessorMap<T> map,ReductionTarget tag) throws InvalidSQLPropertyException {
		super(map);
		if( tag.getReduction() != Reduction.DISTINCT) {
			throw new ConsistencyError("Wrong Reduction");
		}
		setMapper(new DistinctMapper(getContext(), map.getSQLValue(tag.getExpression())));
	}

}
