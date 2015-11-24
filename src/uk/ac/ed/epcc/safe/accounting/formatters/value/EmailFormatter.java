// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.session.AppUser;
@uk.ac.ed.epcc.webapp.Version("$Id: EmailFormatter.java,v 1.5 2014/09/15 14:32:23 spb Exp $")

@Description("Format an AppUser as their Email address")
public class EmailFormatter implements DomFormatter<AppUser> {

	public Class<AppUser> getTarget() {
		return AppUser.class;
	}

	public Node format(Document doc, AppUser user) throws Exception {
		if(user==null){
			return null;
		}
		String email = user.getEmail();
		if( email == null ){
			email="Unknown";
		}
		return doc.createTextNode(email);
	}

}