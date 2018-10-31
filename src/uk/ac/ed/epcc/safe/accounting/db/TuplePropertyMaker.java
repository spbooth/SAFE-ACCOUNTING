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

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.SetMaker;
import uk.ac.ed.epcc.webapp.model.data.Repository;
import uk.ac.ed.epcc.webapp.model.data.SetMapper;



public class TuplePropertyMaker<T extends PropertyTupleFactory.PropertyTuple,PT> extends SetMaker<T, PT> {
	private final PropertyTupleFactory fac;
	public TuplePropertyMaker(AccessorMap<T> map,PropertyTupleFactory fac,PropertyTag<PT> propertyTag, boolean distinct) throws InvalidSQLPropertyException {
		super(map.getContext(),map.getTarget());			
		SQLValue<PT> sqlAccessor = map.getSQLValue(propertyTag);
		if( sqlAccessor == null ){
			throw new InvalidSQLPropertyException(propertyTag);
		}
		SetMapper<PT> mapper = new SetMapper<>(sqlAccessor);					
		setMapper(mapper);			
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
	protected Set<Repository> getSourceTables() {
		return fac.getSourceTables();
	}

}