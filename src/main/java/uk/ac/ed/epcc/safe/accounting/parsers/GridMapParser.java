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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.UnixStreamLineSpliter;
import uk.ac.ed.epcc.webapp.AppContext;


/** Parser for a Globus Grid-map file.
 * 
 * 
 * @author spb
 *
 */
public class GridMapParser extends AbstractPropertyContainerParser {

	public GridMapParser(AppContext conn) {
		super(conn);
	}

	public static final PropertyRegistry gridmap_reg = new PropertyRegistry("gridmap", "Properties from the gridmap file");
	@AutoTable(length=64)
	public static final PropertyTag<String> GRIDMAP_USER = new PropertyTag<>(gridmap_reg, "UserName",String.class);
	@AutoTable(length=128)
	public static final PropertyTag<String> GRIDMAP_DN = new PropertyTag<>(gridmap_reg, "Dn",String.class);
	private static final Pattern parse_pattern = Pattern.compile("\"(.+)\"\\s+(\\w+)");
	@Override
	public boolean parse(DerivedPropertyMap map, String record)
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

	@Override
	public PropertyFinder initFinder( PropertyFinder prev,
			String table) {
		return gridmap_reg;
	}

	@Override
	public Iterator<String> splitRecords(InputStream update)
			throws AccountingParseException {
		try {
			return new UnixStreamLineSpliter(getContext(),update);
		} catch (IOException e) {
			throw new AccountingParseException(e);
		}
	}

}