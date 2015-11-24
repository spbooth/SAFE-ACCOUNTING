// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;


import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.data.reference.ReferenceProvider;
@uk.ac.ed.epcc.webapp.Version("$Id: ReferenceFormatter.java,v 1.5 2014/09/15 14:32:23 spb Exp $")

@Description("generate a type safe reference string")
public class ReferenceFormatter implements DomFormatter<ReferenceProvider> {

	public Class<ReferenceProvider> getTarget() {
		return ReferenceProvider.class;
	}

	public Node format(Document doc, ReferenceProvider value) throws Exception {
		if(value == null){
			return null;
		}
		IndexedReference ref = value.getReference();
		if( ref.isNull()){
			return null;
		}
		return doc.createTextNode(ref.toString());
	}

}