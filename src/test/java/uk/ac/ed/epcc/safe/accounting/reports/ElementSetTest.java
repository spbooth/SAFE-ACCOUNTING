package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ElementSetTest {

	public ElementSetTest() {
		// TODO Auto-generated constructor stub
	}

	
	@Test
	public void testOrder() throws ParserConfigurationException {
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = fac.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element root = doc.createElement("Root");
		doc.appendChild(root);
		
		Element a = doc.createElement("A");
		root.appendChild(a);
		a.appendChild(doc.createTextNode("This is a"));
		Element b = doc.createElement("B");
		root.appendChild(b);
		b.appendChild(doc.createTextNode("This is B"));
		Element c = doc.createElement("C");
		root.appendChild(c);
		c.appendChild(doc.createTextNode("This is C"));
		Element d = doc.createElement("D");
		root.appendChild(d);
		d.appendChild(doc.createTextNode("This is D"));
		ElementSet set = new ElementSet();
		set.add(c);
		set.add(a);
		set.add(c);
		set.add(d);
		
		
		assertEquals(3, set.size());
		assertEquals(a, set.first());
		assertEquals(d, set.last());
		assertTrue(set.contains(c));
		
	}
	
	@Test
	public void testAncestor() throws ParserConfigurationException {
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = fac.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element root = doc.createElement("Root");
		doc.appendChild(root);
		
		
		Element a = doc.createElement("A");
		root.appendChild(a);
		a.appendChild(doc.createTextNode("This is a"));
		Element b = doc.createElement("B");
		root.appendChild(b);
		b.appendChild(doc.createTextNode("This is B"));
		Element c = doc.createElement("C");
		root.appendChild(c);
		c.appendChild(doc.createTextNode("This is C"));
		
		Element b2 = doc.createElement("B2");
		b.appendChild(b2);
		
		Element b3 = doc.createElement("B3");
		b2.appendChild(b3);
		
		ElementSet result = ElementSet.ancestors(b3);
		assertEquals(3, result.size());
		assertTrue(result.contains(root));
		assertTrue(result.contains(b));
		assertTrue(result.contains(b2));
		assertEquals(root,result.first());
		assertEquals(b2,result.last());
		
	}
}
