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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.WebNameFinder;
/** Certificate authenticated accounting {@link AppUser}.
 * 
 * 
 * @author spb
 *
 */


public class CertificatePerson extends PropertyPerson {

	public final Pattern subject_pattern = Pattern.compile("CN=([\\w\\s@.:;_-]+)", Pattern.CASE_INSENSITIVE);
	protected CertificatePerson(CertificatePersonFactory<?> fac, Record res) {
		super(fac, res);
		
	}
	public String getDN(){
		return getRealmName(WebNameFinder.WEB_NAME);
	}
	@Override
	public String getName(){
		String name = getDN();
		if( name == null){
			return null;
		}
		Matcher m = subject_pattern.matcher(name);
		if( m.find()){
			return m.group(1);
		}
        return name;
	}
	@Override
	public String getIdentifier(int max) {
		String dn = getDN();
		if( dn.length() > max){
			return getName();
		}
		return dn;
	}
  
}