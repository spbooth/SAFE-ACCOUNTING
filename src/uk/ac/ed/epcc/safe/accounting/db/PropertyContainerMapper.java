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
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.GroupingSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.InvalidKeyException;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLGroupMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;

/** Version of the SQLGroupMapper that uses PropertyTag and 
 * AccessorMap
 * @author spb
 *
 */


public class PropertyContainerMapper extends SQLGroupMapper<PropertyContainer> {

	private final List<PropertyTag> fields;
	private final AccessorMap<?> m;
	
	public PropertyContainerMapper(AccessorMap<?> m){
		super(m.getContext());
		this.m=m;
		fields=new LinkedList<>();
	}
	public <N>void addKey(PropertyTag<N> t) throws InvalidSQLPropertyException{
		fields.add(t);
		SQLValue<N> v = m.getSQLValue(t);
		if( !(v instanceof GroupingSQLValue)) {
			throw new InvalidSQLPropertyException(t);
		}
		try {
			addKey((GroupingSQLValue<N>) v,t.getName());
		} catch (InvalidKeyException e) {
			throw new InvalidSQLPropertyException(t);
		}
	}
	public final <N extends Number> void addSum(PropertyTag<N> t) throws InvalidSQLPropertyException {
		fields.add(t);
		addSum(m.getSQLExpression(t), t.getName());
	}
	public PropertyContainer makeDefault() {
		return new PropertyMap();
	}

	@SuppressWarnings("unchecked")
	public PropertyContainer makeObject(ResultSet rs) throws DataException, SQLException {
		PropertyMap res = new PropertyMap();
		int pos=0;
		for(PropertyTag t : fields){
		    res.setProperty(t, getTargetObject(pos++, rs));	
		}
		return res;
	}
	

}