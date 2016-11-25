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