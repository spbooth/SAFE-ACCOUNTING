// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.apps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.db.AccountingClassificationFactory;
import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.model.Classification;
public class ReportGeneratorAppTest extends WebappTestBase{

	
	String dir = "tests/uk/ac/ed/epcc/safe/apps/output/";
	
	@Before
	public void loadData() throws Exception{
		load("Eddie.xml");
	}
	
@Test
	public void testHelpOption() throws Exception
	{		
		ArrayList<String> args = new ArrayList<String>();
		//args.add("ReportGenerator");
		args.add("-h");
		try{
		testReport(args, new File(dir+"ReportGeneratorHelp.txt"));
		}catch(ConsistencyError e){
			assertEquals(e.getMessage(),"Application exit: 0");
		}
		
	}

@Test
public void testHTMLReport() throws Exception {
		ArrayList<String> args = new ArrayList<String>();
		//args.add("--");
		args.add("-r");
		args.add("testReport.xml");
		args.add("-t");
		args.add("html");
		File outputFile = File.createTempFile("report", ".htm");
		args.add("-f");
		args.add(outputFile.getPath());
		//System.out.println(outputFile.getAbsolutePath());
		testReport(args, new File(dir+"ReportGeneratorReport.html"));

	}
	
@Test
public void testPDFReport() throws Exception {
		ArrayList<String> args = new ArrayList<String>();
		//args.add("--");
		args.add("-r");
		args.add("testReport.xml");
		args.add("-t");
		args.add("pdf");
		File outputFile = File.createTempFile("report", ".pdf");
		args.add("-f");
		args.add(outputFile.getPath());
		testReport(args, "%PDF-1.4");

	}
	
@Test
public void testXMLReport() throws Exception {
		ArrayList<String> args = new ArrayList<String>();
		//args.add("--");
		args.add("-r");
		args.add("testReport.xml");
		args.add("-t");
		args.add("xml");		
		File outputFile = File.createTempFile("report", ".xml");
		args.add("-f");
		args.add(outputFile.getPath());
		testReport(args, new File(dir+"ReportGeneratorReport.xml"));

	}
	
@Test
public void testCSVReport() throws Exception {
		ArrayList<String> args = new ArrayList<String>();
		//args.add("--");
		args.add("-r");
		args.add("testReport.xml");
		args.add("-t");
		args.add("csv");
		File outputFile = File.createTempFile("report", ".csv");
		args.add("-f");
		args.add(outputFile.getPath());
		testReport(args, new File(dir+"ReportGeneratorReport.csv"));

	}
	
//	public void testWritingReportToFile() throws Exception {
//		ArrayList<String> args = new ArrayList<String>();
//		//args.add("--");
//		args.add("-r");
//		args.add("testReport.xml");
//		args.add("-f");
//		args.add("testReport.html");		
//		testReport(args, new File(dir+"ReportGeneratorReport.html"));
//
//	}
	@Test
	public void testReportWithParams() throws Exception
	{		
		AccountingClassificationFactory institute_fac = new AccountingClassificationFactory(ctx, "Institute");
		Classification institute = institute_fac.findFromString("Infrastructure and Environment");
		AccountingClassificationFactory school_fac = new AccountingClassificationFactory(ctx, "School");
		Classification school = school_fac.findFromString("Biological Sciences");
		
		ArrayList<String> args = new ArrayList<String>();
		//args.add("--");
		args.add("-r");
		args.add("testParameters.xml");
		args.add("-SMyLong=1");
		args.add("-SMyInteger=1");
		args.add("-SMyFloat=1");
		args.add("-SMyDouble=1");
		args.add("-SMyString=1");
		args.add("-SMyDate=2010-10-10");
		args.add("-SMyTimeStamp=2010-10-10 10:10:10");
		args.add("-SMyBoolean=true");
		args.add("-SMyMachine=1");
		args.add("-SDerived="+institute.getID());
		args.add("-SDoubleDerived="+school.getID());
		args.add("-SMyLabel=blah");
		args.add("-SDefault=Bob");
		args.add("-SReadOnly=Bob");
		args.add("-SLongString=blah blah blah");
		args.add("-SListString=bar");
		args.add("-t");
		args.add("html");
		File outputFile = File.createTempFile("ReportWithParams", ".htm");
		args.add("-f");
		args.add(outputFile.getPath());
		testReport(args, new File(dir+"ReportGeneratorParams.html"));
	}
	
@Test
	public void testChartReport() throws Exception {
		ArrayList<String> args = new ArrayList<String>();
		//args.add("--");
		args.add("-r");
		args.add("testCharts.xml");
		args.add("-t");
		args.add("html");
		File outputFile = File.createTempFile("ReportGeneratorCharts", ".htm");
		args.add("-f");
		args.add(outputFile.getPath());
		testReport(args, new File(dir+"ReportGeneratorCharts.html"));

    }
	protected void testReport(ArrayList<String> args, File outputFile) throws Exception
	{
		testReport(args, TestDataHelper.readFileAsString(outputFile).replaceAll("<!--.*-->\\s*\n?", ""));
	}
	
	protected void testReport(ArrayList<String> args, String expectedOutput) throws Exception
	{		
		File outputFile = null;
		if (args.contains("-f")) {			
			outputFile = new File(args.get(args.indexOf("-f")+1));
			
		}
		
		if (outputFile == null) {
			TestDataHelper.redirectStdOut();
		}
		ReportGeneratorApp app = new ReportGeneratorApp(ctx);
		app.run( new LinkedList<String>(args) );
		
		String stdOutString;		
		if (outputFile == null) {
			stdOutString = TestDataHelper.readStdOut();
			TestDataHelper.resetStdOut();
			
		} else {
			stdOutString = TestDataHelper.readFileAsString(outputFile);
			//outputFile.delete();
			
		}
		stdOutString=stdOutString.replace(ctx.getInitParameter("java.io.tmpdir","/tmp"), "/tmp").replaceAll("<!--.*-->\\s*\n?", "");
		System.err.print(stdOutString);
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, stdOutString),
				stdOutString.contains(expectedOutput));
		
	}
	
}