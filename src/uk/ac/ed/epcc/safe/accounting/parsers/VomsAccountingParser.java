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

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.webapp.AppContext;



public class VomsAccountingParser extends RegexpParser {
	public VomsAccountingParser(AppContext conn) {
		super(conn);
	}

	public static final PropertyRegistry voms_reg = new PropertyRegistry("voms", "Properties from the voms_account log");
	@AutoTable(target=String.class,length=128)
	@Regexp("dn:\"([^\"]*)\"")
	public static final PropertyTag<String> VOMS_DN=new PropertyTag<>(voms_reg, "Dn",String.class);
	@AutoTable(target=String.class,length=128,unique=true)
	@Regexp("jid:(\\d+-\\d+-\\d+\\.\\d+:\\d+:\\d+\\.\\d+\\.\\d+)")
	public static final PropertyTag<String> VOMS_ID=new PropertyTag<>(voms_reg, "Id",String.class);
	@AutoTable(target=String.class)
	@Regexp("jid:\\d+-\\d+-\\d+\\.\\d+:\\d+:\\d+\\.\\d+\\.\\d+\\.([^ ]*)")
	public static final PropertyTag<String> VOMS_SUBMISSION=new PropertyTag<>(voms_reg, "Submission",String.class);
	@Regexp("uid:(\\d+)")
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> VOMS_UID = new PropertyTag<>(voms_reg, "UID", Integer.class);
    @AutoTable(target=String.class,length=128)
	@Regexp("vo0:\"([^\"]*)\"")
	public static final PropertyTag<String> VOMS_VO0 = new PropertyTag<>(voms_reg, "VO0",String.class);
	@Regexp("poolindex:\"([^\"]*)\"")
	public static final PropertyTag<String> VOMS_POOLINDEX = new PropertyTag<>(voms_reg, "PoolIndex",String.class);
	
	static{
		voms_reg.lock();
	}
	@Override
	public PropertyFinder initFinder(PropertyFinder prev,
			String table) {
		return voms_reg;
	}
	

}