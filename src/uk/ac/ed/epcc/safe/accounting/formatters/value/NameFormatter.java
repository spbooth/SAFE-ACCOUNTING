// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.security.Principal;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Description;
@uk.ac.ed.epcc.webapp.Version("$Id: NameFormatter.java,v 1.5 2014/09/15 14:32:23 spb Exp $")

@Description("get the principal name")
public class NameFormatter implements DomFormatter<Principal> {

	public Class<Principal> getTarget() {
		return Principal.class;
	}

	public Node format(Document doc, Principal value) throws Exception {
		if(value == null){
			return null;
		}
		return doc.createTextNode(value.getName());
	}

}