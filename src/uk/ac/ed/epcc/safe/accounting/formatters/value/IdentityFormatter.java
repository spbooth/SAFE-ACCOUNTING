// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.forms.Identified;
@uk.ac.ed.epcc.webapp.Version("$Id: IdentityFormatter.java,v 1.1 2015/05/28 10:38:04 spb Exp $")

@Description("Generate the id number of the target")
public class IdentityFormatter implements DomFormatter<Identified> {

	public Class<Identified> getTarget() {
		return Identified.class;
	}

	public Node format(Document doc, Identified value) throws Exception {
		if(value == null){
			return null;
		}
		return doc.createTextNode(value.getIdentifier());
	}

}