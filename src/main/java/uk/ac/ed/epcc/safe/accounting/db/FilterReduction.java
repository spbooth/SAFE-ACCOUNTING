//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.expr.ReductionMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;



public class FilterReduction<T,R> extends AccessorMapFilterFinder<T, R> {
	public FilterReduction(AccessorMap<T> map,Class<R> target, Reduction red, R def, SQLExpression expr) throws InvalidSQLPropertyException {
		super(map);
		setMapper(new ReductionMapper<>(map.getContext(),target,red,def, expr));
	}
}