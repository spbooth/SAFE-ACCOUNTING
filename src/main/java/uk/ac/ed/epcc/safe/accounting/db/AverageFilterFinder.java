package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.webapp.AverageValue;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.expr.AverageValueSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.expr.ValueResultMapper;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterFinder;
/** A {@link FilterFinder} that generates a {@link AverageValue} value
 * from the result of the reduction
 * 
 * 
 * @author Stephen Booth
 *
 * @param <T> type of factory
 */
public class AverageFilterFinder<T> extends AccessorMapFilterFinder<T, AverageValue> {

	public AverageFilterFinder(AccessorMap<T> map,ReductionTarget tag) throws InvalidSQLPropertyException {
		super(map);
		if( tag.getReduction() != Reduction.AVG) {
			throw new ConsistencyError("Wrong Reduction");
		}
		setMapper(new ValueResultMapper<AverageValue>(new AverageValueSQLValue(map.getSQLExpression(tag.getExpression()))));
	}

}
