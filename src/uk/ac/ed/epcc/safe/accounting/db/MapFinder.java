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

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.jdbc.expr.MapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.FieldValue;
import uk.ac.ed.epcc.webapp.model.data.Repository;



public class MapFinder<T extends DataObject&ExpressionTarget,K,R> extends FilterFinder<T, Map<K,R>> {
	private final Repository res;
	public MapFinder(AccessorMap<T> map,Repository res, PropertyTag<K> key,
			PropertyTag<R> value) throws InvalidSQLPropertyException {
		super(map.getContext(),map.getTarget(), true); // can return null
		this.res=res;
		assert(key != null);
		assert(value != null);
		
		SQLValue<K> a = map.getSQLValue(key);
		String key_name=null;
		if( ! (a instanceof FieldValue)){
			key_name = key.toString();
		}
		
		SQLValue<R> e  = map.getSQLValue(value);
		String value_name=null;
		if( ! (e instanceof FieldValue)){
			value_name=value.toString();
		}
		MapMapper<K,R> mapper = new MapMapper<K,R>(map.getContext(),a,key_name,e,value_name);
		setMapper(mapper);
	}
	@Override
	protected void addSource(StringBuilder sb) {
		res.addSource(sb, true);
		
	}
	@Override
	protected String getDBTag() {
		return res.getDBTag();
	}
}