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

import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.SetMaker;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository;
import uk.ac.ed.epcc.webapp.model.data.SetMapper;



public class PropertyMaker<T extends DataObject,PT> extends SetMaker<T, PT> {
	private final Repository res;
	public PropertyMaker(AccessorMap<T> map,Repository res,PropExpression<PT> expr, boolean distinct) throws InvalidSQLPropertyException {
		super(map.getContext(),map.getFilterTag());			
		SQLValue<PT> sqlAccessor = map.getSQLValue(expr);
		if( sqlAccessor == null ){
			throw new InvalidSQLPropertyException(expr);
		}
		SetMapper<PT> mapper = new SetMapper<>(sqlAccessor);					
		setMapper(mapper);			
		this.res=res;
	}
	@Override
	protected void addSource(StringBuilder sb) {
		res.addSource(sb, true);
		
	}
	@Override
	protected String getDBTag() {
		return res.getDBTag();
	}
	@Override
	protected Set<Repository> getSourceTables() {
		HashSet<Repository> set = new HashSet<>();
		set.add(res);
		return set;
	}

}