// Copyright - The University of Edinburgh 2011
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
@uk.ac.ed.epcc.webapp.Version("$Id: CertificatePerson.java,v 1.12 2015/10/26 10:07:20 spb Exp $")

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