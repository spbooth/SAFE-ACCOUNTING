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
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;


/** A placeholder do-nothing parser.
 * 
 * @author spb
 *
 */
public class NullParser implements PropertyContainerParser<String> {

	
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		return prev;
	}


	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		return previous;
	}


	public void startParse(PropertyContainer static_props) throws Exception {
		
	}


	public String endParse() {
		return null;
	}

	public TableSpecification modifyDefaultTableSpecification(AppContext conn,
			TableSpecification t, PropExpressionMap map,
			String table_name) {
		return t;
	}

	@Override
	public boolean parse(DerivedPropertyMap map, String record)
			throws AccountingParseException {
		return false;
	}


	public Iterator<String> splitRecords(String update)
			throws AccountingParseException {
		return null;
	}


	public Set<PropertyTag> getDefaultUniqueProperties() {
		return new HashSet<PropertyTag>();
	}


	@Override
	public String formatRecord(String record) {
		return record;
	}


	@Override
	public String getRecord(String text) {
		return text;
	}

}