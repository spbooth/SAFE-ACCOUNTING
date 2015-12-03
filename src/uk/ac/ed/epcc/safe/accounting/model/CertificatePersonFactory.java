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
package uk.ac.ed.epcc.safe.accounting.model;

import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.session.WebNameFinder;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
/** Factory for {@link CertificatePerson} objects.
 * DN names may be provided in either Web or LDap formats but are
 * normalised to the Web format internally
 * 
 * @author spb
 * @param <P> certificate person type
 *
 */


public class CertificatePersonFactory<P extends CertificatePerson> extends PropertyPersonFactory<P> {
	
	
	@Override
	protected String getDefaultRealm() {
		return WebNameFinder.WEB_NAME;
	}


	// This should take precidence over any other static NameFinder using WebName as the realm
	// as superclasses are constructed first
	public final DNNameFinder<P> finder = new DNNameFinder<P>(this, WebNameFinder.WEB_NAME);

	public CertificatePersonFactory() {
		super();
	}

	public CertificatePersonFactory(AppContext ctx, String table) {
		super(ctx, table);
	}

	public CertificatePersonFactory(AppContext ctx) {
		super(ctx);
	}
	

	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new CertificatePerson(CertificatePersonFactory.this, res);
	}
}