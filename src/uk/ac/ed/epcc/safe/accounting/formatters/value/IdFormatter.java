// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.Indexed;
@uk.ac.ed.epcc.webapp.Version("$Id: IdFormatter.java,v 1.5 2014/09/15 14:32:23 spb Exp $")

@Description("Generate the id number of the target")
public class IdFormatter implements DomFormatter<Indexed> {

	public Class<Indexed> getTarget() {
		return Indexed.class;
	}

	public Node format(Document doc, Indexed value) throws Exception {
		if(value == null){
			return null;
		}
		return doc.createTextNode(Integer.toString(value.getID()));
	}

}