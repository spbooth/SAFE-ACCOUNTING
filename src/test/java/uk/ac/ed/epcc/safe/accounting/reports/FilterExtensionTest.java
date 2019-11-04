// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;

import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
public class FilterExtensionTest extends ExtensionTestCase {

	
	
	

	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterNameParse() throws Exception {			
		testFilter("csv", "testNameParseFilters.xml",
				new File(getOutputDir()+"FilterNameParse.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterIdParse() throws Exception {			
		testFilter("csv", "testIdParseFilters.xml",
				new File(getOutputDir()+"FilterIdParse.csv"));
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterEQ() throws Exception {			
		testFilter("csv", "testEQFilters.xml",
				new File(getOutputDir()+"FilterEQ.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterNE() throws Exception {			
		testFilter("csv", "testNEFilters.xml",
				new File(getOutputDir()+"FilterNE.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterGT() throws Exception {			
		testFilter("csv", "testGTFilters.xml",
				new File(getOutputDir()+"FilterGT.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterGE() throws Exception {			
		testFilter("csv", "testGEFilters.xml",
				new File(getOutputDir()+"FilterGE.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterLT() throws Exception {			
		testFilter("csv", "testLTFilters.xml",
				new File(getOutputDir()+"FilterLT.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterLE() throws Exception {			
		testFilter("csv", "testLEFilters.xml",
				new File(getOutputDir()+"FilterLE.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterAnd() throws Exception {			
		testFilter("csv", "testAndFilters.xml",
				new File(getOutputDir()+"FilterAnd.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterOr() throws Exception {			
		testFilter("csv", "testOrFilters.xml",
				new File(getOutputDir()+"FilterOr.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterWithDerivedProperty() throws Exception {			
		testFilter("csv", "testDerivedPropertyFilters",
				new File(getOutputDir()+"FilterWithDerivedProperty.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterWithDoubleDerivedProperty() throws Exception {			
		testFilter("csv", "testDoubleDerivedFilters.xml",
				new File(getOutputDir()+"FilterWithDoubleDerivedProperty.csv"));
	}
	
	

	protected void testFilter(String reportType, String templateName,File outputFile) 
	throws Exception 
	{		
		testFlter(reportType, templateName,TestDataHelper.readFileAsString(outputFile));
	
	}
	
	/** tests filters can be parsed/serialised then re-parsed to the same filer 
	 * @throws Exception 
	 * @throws DOMException 
	 * 
	 */
	protected void testRoundTrip(ReportBuilder builder) throws DOMException, Exception {
		FilterExtension fil= new FilterExtension(getContext());
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
				docBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		TransformerFactory fac = TransformerFactory.newInstance();
		Transformer t = fac.newTransformer();
		t.transform(builder.getTemplateSource(), new DOMResult(doc));
		NodeList list = doc.getElementsByTagNameNS(FilterExtension.FILTER_LOC, FilterExtension.FILTER_ELEMENT);
		for( int i=0 ; i<list.getLength();i++) {
			RecordSet parsed = fil.makeFilter(list.item(i));

			DocumentFragment serial = fil.formatRecordSet(parsed);
			t.transform(new DOMSource(serial), new StreamResult(System.out));
			RecordSet second = fil.makeFilter(serial);

			assertEquals("REcordSet not regenerated",parsed,second);
			assertFalse(fil.getErrors().hasError());
		}
	}
	protected void testFlter(String type, String templateName,String expectedOutput)
		throws Exception 
	{
	
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String, Object> params = new HashMap<>();
		
		ReportBuilderTest.setupParams(ctx, params);
		
		ReportBuilder reportBuilder = 
			new ReportBuilder(ctx, templateName,"report.xsd");
		ReportType reportType=reportBuilder.getReportTypeReg().getReportType(type);
		params.put(ReportTypeRegistry.REPORT_TYPE_PARAM, reportType);
		reportBuilder.setupExtensions(reportType,params);
		reportBuilder.buildReportParametersForm(form, params);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		//System.out.println(out.toString());
		// Check it was correctly formatted.
		String outstring = normalise(out.toString());
		expectedOutput=normalise(expectedOutput);
		assertTrue("Report wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, outstring),
				TestDataHelper.compareUnordered(outstring, expectedOutput));
		testRoundTrip(reportBuilder);
	}

}