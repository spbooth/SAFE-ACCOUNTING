package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.parsers.TestParser;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.RecordSelectException;
import uk.ac.ed.epcc.safe.accounting.selector.NullSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OrRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RelationClause;
import uk.ac.ed.epcc.safe.accounting.selector.RelationshipClause;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.Duration;

public class FilterSerialiseTests extends WebappTestBase {

	private static final String EXPECTED = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Filter xmlns=\"http://safe.epcc.ed.ac.uk/filter\"><Producer>TestRecord</Producer><TimeBounds><Property>time:CompletedTimestamp</Property></TimeBounds><And><EQ><Property>test:String</Property><Value>Ego et errata</Value></EQ><LE><Property>test:Double</Property><Value>14.0</Value></LE><GE><Property>test:Duration</Property><Value>0:5:0</Value></GE><GE><Property>test:Date</Property><Value>2019-12-12 09:30:00</Value></GE><NE><Property>test:Number</Property><Property2>test:Double</Property2></NE><Relationship>Peon</Relationship><Null><Property>test:Date</Property></Null><NotNull><Property>test:Animals</Property></NotNull><Or><EQ><Property>test:Animals</Property><Value>Penguin</Value></EQ><EQ><Property>test:Animals</Property><Value>Wombat</Value></EQ></Or></And></Filter>";

	@Test
	public void testSerialise() throws DOMException, Exception {
		AccountingService serv = ctx.getService(AccountingService.class);
		
		UsageProducer test = serv.getUsageProducer("TestRecord");
		
		assertNotNull(test);
		
		RecordSet set = new RecordSet(serv);
		set.setUsageProducer("TestRecord");
		set.addRecordSelector(new SelectClause<String>(TestParser.STRING_PROP, "Ego et errata"));
		set.addRecordSelector(new SelectClause(TestParser.DOUBLE_PROP,MatchCondition.LE,14.0));
		set.addRecordSelector(new SelectClause(TestParser.DURATION_PROP, MatchCondition.GE, new Duration(300)));
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2019, Calendar.DECEMBER, 12, 9, 30);
		
		set.addRecordSelector(new SelectClause(TestParser.DATE_PROP, MatchCondition.GE, cal.getTime()));
		
		set.addRecordSelector(new RelationClause(TestParser.NUMBER_PROP, MatchCondition.NE, TestParser.DOUBLE_PROP));
		set.addRecordSelector(new RelationshipClause("Peon"));
		set.addRecordSelector(new NullSelector(TestParser.DATE_PROP, true));
	    set.addRecordSelector(new NullSelector(TestParser.ANIMAL_TAG, false));
	    OrRecordSelector or = new OrRecordSelector();
	    or.add(new SelectClause(TestParser.ANIMAL_TAG, TestParser.Animals.Penguin));
	    or.add(new SelectClause(TestParser.ANIMAL_TAG, TestParser.Animals.Wombat));
	    set.addRecordSelector(or);
	    
	    FilterExtension fil= new FilterExtension(getContext());
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
				docBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		TransformerFactory fac = TransformerFactory.newInstance();
		Transformer t = fac.newTransformer();
		
		DocumentFragment frag = fil.formatRecordSet(set);
		doc.appendChild(doc.importNode(frag, true));
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		t.transform(new DOMSource(doc),new StreamResult(out));
		assertEquals(EXPECTED,
				out.toString().replace(" standalone=\"no\"", ""));
		NodeList list = doc.getElementsByTagNameNS(FilterExtension.FILTER_LOC, FilterExtension.FILTER_ELEMENT);
		for( int i=0 ; i<list.getLength();i++) {
			RecordSet parsed = fil.makeFilter(list.item(i));

			DocumentFragment serial = fil.formatRecordSet(parsed);
			assertNotNull(serial);
			assertTrue(serial.hasChildNodes());
			ByteArrayOutputStream out2 = new ByteArrayOutputStream();
			t.transform(new DOMSource(serial), new StreamResult(out2));
			assertEquals(EXPECTED,
					out2.toString().replace(" standalone=\"no\"", ""));
			RecordSet second = fil.makeFilter(serial);

			assertEquals("parsed does not match original",set,parsed);
			assertEquals("REcordSet not regenerated",parsed,second);
			assertFalse(fil.getErrors().hasError());
		}
		
		
	}
	
}
