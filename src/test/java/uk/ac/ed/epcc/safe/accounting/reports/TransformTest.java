// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.TestCase;

public class TransformTest {
	@Test
	public void testTransform() throws TransformerException{
		InputStream is = getClass().getResourceAsStream("input.xml");
		assertNotNull(is);
		Source input = new StreamSource(is);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Source xslSource = new StreamSource(getClass().getResourceAsStream("test.xsl"));
		Transformer transformer = tFactory.newTransformer(xslSource);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		transformer.transform(input, new StreamResult(out));
		String result=out.toString();
		//System.out.println(result);
		assertTrue(result.contains("Test 1 fred"));
		assertTrue(result.contains("Test 2 boris"));
		assertTrue(result.contains("Test 1 tim"));
		assertTrue(result.contains("Test 2 topsy"));
		assertTrue(result.contains("Start Block"));
		assertTrue(result.contains("Definition test [igor][cuddles] !"));
	}

}