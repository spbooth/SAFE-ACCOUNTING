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
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.AppContext;


/** Parser for a Globus Grid-map file.
 * 
 * 
 * @author spb
 *
 */
public class GridMapParser extends AbstractPropertyContainerParser {

	public static final PropertyRegistry gridmap_reg = new PropertyRegistry("gridmap", "Properties from the gridmap file");
	public static final PropertyTag<String> GRIDMAP_USER = new PropertyTag<String>(gridmap_reg, "UserName",String.class);
	public static final PropertyTag<String> GRIDMAP_DN = new PropertyTag<String>(gridmap_reg, "Dn",String.class);
	private static final Pattern parse_pattern = Pattern.compile("\"(.+)\"\\s+(\\w+)");
	public boolean parse(PropertyMap map, String record)
			throws AccountingParseException {
		Matcher m = parse_pattern.matcher(record);
		if( m.matches()){
			String dn = m.group(1);
			String name=m.group(2);
			map.setProperty(GRIDMAP_DN, dn);
			map.setProperty(GRIDMAP_USER, name);
			return true;
		}
		return false;
	}

	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		return gridmap_reg;
	}

	@Override
	public Iterator<String> splitRecords(String update)
			throws AccountingParseException {
		return new UnixFileSplitter(update);
	}

}