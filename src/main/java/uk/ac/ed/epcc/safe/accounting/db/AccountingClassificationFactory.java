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

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
/** Factory class for {@link AccountingClassification} objects.
 * 
 * By default the properties are generated from the Database fields but additional properties can be
 * defined as derived properties
 * @author spb
 *
 * @param <T>
 */


public class AccountingClassificationFactory<T extends AccountingClassification>
		extends PropertyClassificationFactory<T> {
	
	@Override
	protected T makeBDO(Record res) throws DataFault {
		return (T) new AccountingClassification(this, res);
	}

	@Override
	public Class<T> getTarget() {
		return (Class<T>) AccountingClassification.class;
	}

	public AccountingClassificationFactory(AppContext c, String table) {
		super();
		setContext(c, table);
		// Logger log = getLogger();
		
	}
	
}