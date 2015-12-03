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

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpressionFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.DataObject;



public class PropertyMatchfilter<T extends DataObject&ExpressionTarget,P> extends SQLExpressionFilter<T,P>{
	public PropertyMatchfilter(AccessorMap<T> map,PropertyTag<P> key, MatchCondition match ,PropertyContainer c) throws InvalidSQLPropertyException, InvalidExpressionException{
		super(map.getTarget(),map.getSQLExpression(key),match,c.getProperty(key));
	}
	public PropertyMatchfilter(AccessorMap<T> map,PropertyTag<P> key,MatchCondition match, P value) throws InvalidSQLPropertyException{
		super(map.getTarget(),map.getSQLExpression(key),match,value);
	}
}