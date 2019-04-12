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

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.webapp.AppContext;

/** Parser for standard unix password files 
 * 
 * @author spb
 *
 */


public class PasswdParser extends AbstractPropertyContainerParser {

	public PasswdParser(AppContext conn) {
		super(conn);
	}



	private static final PropertyRegistry passwd = new PropertyRegistry("passwd", "Properties from a unix passwd file");
	public static final PropertyTag<String> USERNAME = new PropertyTag<>(passwd,"Username",String.class,"login name of user");
	public static final PropertyTag<String> PW = new PropertyTag<>(passwd,"Password",String.class,"encrypted password");
	public static final PropertyTag<Number> UID = new PropertyTag<>(passwd,"UID",Number.class,"numerical user id");
	public static final PropertyTag<Number> GID = new PropertyTag<>(passwd,"GID",Number.class,"primary group id");
	@AutoTable
	public static final PropertyTag<String> GECOS = new PropertyTag<>(passwd,"FullName",String.class,"Full name of user");
	public static final PropertyTag<String> DIR = new PropertyTag<>(passwd,"HomeDir",String.class,"Home directory of user");
	public static final PropertyTag<String> SHELL = new PropertyTag<>(passwd,"Shell",String.class,"Login shell of user");
	
	@Override
	public boolean parse(DerivedPropertyMap map, String record)
			throws AccountingParseException {
		if (record.trim().length() == 0 || record.startsWith("#")) {
			return false;
		}
		String fields[] = record.split(":");
		if (fields.length != 7) {
			throw new AccountingParseException("Wrong number of fields");
		}
		try{
		Long uid = Long.valueOf(fields[2]);
		Long gid = Long.valueOf(fields[3]);
		map.setProperty(USERNAME, fields[0]);
		map.setProperty(PW, fields[1]);

		map.setProperty(UID, uid);

		map.setProperty(GID, gid);
		map.setProperty(GECOS, fields[4]);
		map.setProperty(DIR, fields[5]);
		map.setProperty(SHELL, fields[6]);
		}catch(NumberFormatException e){
			throw new AccountingParseException("Bad number format", e);
		}
		return true;
	}

	

	

	@Override
	public PropertyFinder initFinder(PropertyFinder prev,
			String table) {
		return passwd;
	}
	

	public Set<PropertyTag> getDefaultUniqueProperties() {
		return null;
	}

}