// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
@uk.ac.ed.epcc.webapp.Version("$Id: DomValueFormatter.java,v 1.4 2014/09/15 14:32:23 spb Exp $")


public class DomValueFormatter<T> implements DomFormatter<T> {

	private final ValueFormatter<T> parser;

	public DomValueFormatter(ValueFormatter<T> p){
		this.parser=p;
	}
	public Class<T> getTarget() {
		return parser.getType();
	}

	public Node format(Document doc, T value) {
		return doc.createTextNode(parser.format(value));
	};
	
}