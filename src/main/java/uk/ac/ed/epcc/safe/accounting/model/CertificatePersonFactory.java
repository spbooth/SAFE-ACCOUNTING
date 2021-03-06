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

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.session.WebNameFinder;
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
	public String getDefaultRealm() {
		return WebNameFinder.WEB_NAME;
	}


	// This should take precidence over any other static NameFinder using WebName as the realm
	// as superclasses are constructed first
	public final DNNameFinder<P> dn_finder = new DNNameFinder<>(this, WebNameFinder.WEB_NAME);

	public CertificatePersonFactory() {
		super();
	}

	public CertificatePersonFactory(AppContext ctx, String table) {
		this();
		// Must call setContext explicitly to ensure dn_finder constructed
		setContext(ctx, table);
	}

	public CertificatePersonFactory(AppContext ctx) {
		super(ctx);
	}
	

	@Override
	protected P makeBDO(Record res) throws DataFault {
		return (P) new CertificatePerson(CertificatePersonFactory.this, res);
	}
}