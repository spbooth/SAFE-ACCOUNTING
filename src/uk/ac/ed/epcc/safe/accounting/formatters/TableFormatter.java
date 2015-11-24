package uk.ac.ed.epcc.safe.accounting.formatters;

import java.text.NumberFormat;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.safe.accounting.formatters.value.DomFormatter;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.content.TableXMLFormatter;
import uk.ac.ed.epcc.webapp.content.XMLDomBuilder;

public class TableFormatter implements DomFormatter<Table>{

	
	public Class<? super Table> getTarget() {
		return Table.class;
	}

	
	public Node format(Document doc, Table value) throws Exception {
		DocumentFragment frag = doc.createDocumentFragment();
		XMLDomBuilder xb = new XMLDomBuilder(frag);
	
		TableXMLFormatter txf = new TableXMLFormatter(xb, NumberFormat.getInstance(),"auto");
		txf.add(value);
		return frag;
	}

}
