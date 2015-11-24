// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.WebNameFinder;
@uk.ac.ed.epcc.webapp.Version("$Id: WebNameFormatter.java,v 1.6 2015/10/26 10:07:21 spb Exp $")

@Description("Formats an AppUser as their webname")
public class WebNameFormatter implements DomFormatter<AppUser> {

	public Class<AppUser> getTarget() {
		return AppUser.class;
	}

	public Node format(Document doc, AppUser user) throws Exception {
		if(user==null){
			return null;
		}
		String webName = user.getRealmName(WebNameFinder.WEB_NAME);
		if( webName == null ){
			webName="Unknown";
		}
		return doc.createTextNode(webName);
	}

}