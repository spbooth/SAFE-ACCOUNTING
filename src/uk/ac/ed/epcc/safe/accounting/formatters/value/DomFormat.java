// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.text.Format;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
@uk.ac.ed.epcc.webapp.Version("$Id: DomFormat.java,v 1.3 2014/09/15 14:32:23 spb Exp $")


public class DomFormat<T> implements DomFormatter<T> {

	private final Format fmt;
	private final Class<T> clazz;

	public DomFormat(Class<T> clazz,Format f){
		this.fmt=f;
		this.clazz=clazz;
	}
	public Class<T> getTarget() {
		return clazz;
	}

	public Node format(Document doc, T value) {
		return doc.createTextNode(fmt.format(value));
	};
	
}