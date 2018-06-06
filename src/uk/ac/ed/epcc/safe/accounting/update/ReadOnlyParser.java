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
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.table.DateFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;

/** A Placeholder parser for read-only tables 
 * 
 * @author spb
 *
 */


public class ReadOnlyParser extends BaseParser {

	@Override
	public TableSpecification modifyDefaultTableSpecification(AppContext c,
			TableSpecification spec, PropExpressionMap map, String table_name) {
		spec.setField(StandardProperties.STARTED_TIMESTAMP, new DateFieldType(false, new Date(0L)));
		spec.setField(StandardProperties.COMPLETED_TIMESTAMP, new DateFieldType(false, new Date(Long.MAX_VALUE)));
		return spec;
	}
	@Override
	public boolean parse(DerivedPropertyMap map, String record)
			throws AccountingParseException {
		return false;
	}

	@Override
	public PropertyFinder initFinder(AppContext conn, PropertyFinder prev,
			String table) {
		return StandardProperties.time;
	}

	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		return previous;
	}

}