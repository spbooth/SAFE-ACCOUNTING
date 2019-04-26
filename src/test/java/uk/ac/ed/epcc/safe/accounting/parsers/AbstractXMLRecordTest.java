// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.io.StringReader;
import java.util.Iterator;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.ed.epcc.safe.accounting.ogf.ur.XMLSplitter;
import uk.ac.ed.epcc.webapp.AppContext;

public abstract class AbstractXMLRecordTest extends AbstractRecordTestCase {

	public AbstractXMLRecordTest(AppContext c, String machineName,
			String tableName) {
		super(machineName, tableName);
	}

	public Iterator<String> splitRecords(String records) throws Exception {
			XMLSplitter handler = new XMLSplitter();
			
				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(handler);
				parser.parse(new InputSource(new StringReader(records)));
			
	
			return handler.iterator();
		
	}

}