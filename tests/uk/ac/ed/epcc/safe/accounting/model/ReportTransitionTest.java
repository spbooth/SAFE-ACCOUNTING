package uk.ac.ed.epcc.safe.accounting.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringBufferInputStream;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamData;
import uk.ac.ed.epcc.webapp.model.serv.ServeDataProducer;
import uk.ac.ed.epcc.webapp.model.serv.SettableServeDataProducer;
import uk.ac.ed.epcc.webapp.servlet.AbstractTransitionServletTest;
import uk.ac.ed.epcc.webapp.session.SessionDataProducer;
import uk.ac.ed.epcc.webapp.session.SessionService;

public class ReportTransitionTest extends AbstractTransitionServletTest {

	public ReportTransitionTest() {
		// TODO Auto-generated constructor stub
	}

	
	@Test
	public void testDefaultTransition() throws Exception {
		setupPerson("fred@example.com");
		ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(ctx);
		Report report = new Report("TimeChart.xml");
		setTransition(prov, null, report);
		runTransition();
		checkForward("/scripts/transition.jsp");
		checkFormContent("/normalize.xsl", "default_preview.xml");
	}
	
	@Test
	public void initialShow() throws Exception {
		setupPerson("fred@example.com");
		ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(ctx);
		Report report = new Report("SimpleTestReport.xml");
		setTransition(prov, null, report);
		runTransition();
		checkForward("/scripts/transition.jsp");
		checkFormContent("/normalize.xsl", "initial_show.xml");
	}
	
	@Test
	public void testPreview() throws Exception {
		setupPerson("fred@example.com");
		ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(ctx);
		HashMap<String, Object> params=new HashMap<>();
		params.put("Message", "Hello world");
		Report report = new Report("SimpleTestReport.xml",params);
		setTransition(prov, null, report);
		runTransition();
		checkForward("/scripts/transition.jsp");
		checkFormContent("/normalize.xsl", "test_preview.xml");
	}
	
	@Test
	public void testDeveloperPreview() throws Exception {
		SessionService user = setupPerson("fred@example.com");
		user.setTempRole(ReportBuilder.REPORT_DEVELOPER);
		ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(ctx);
		HashMap<String, Object> params=new HashMap<>();
		params.put("Message", "Hello world");
		Report report = new Report("SimpleTestReport.xml",params);
		setTransition(prov, null, report);
		runTransition();
		checkForward("/scripts/transition.jsp");
		checkFormContent("/normalize.xsl", "test_developer_preview.xml");
		
		SettableServeDataProducer prod = getContext().makeObjectWithDefault(
				SettableServeDataProducer.class, SessionDataProducer.class, ServeDataProducer.DEFAULT_SERVE_DATA_TAG);
		assertNotNull(prod);
		LinkedList<String> path = new LinkedList<>();
		path.add("1");
		path.add("logdata.txt");
		MimeStreamData data = prod.getData(user, path);
		assertNotNull(data);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		data.write(out);
		String result = out.toString().trim();
		System.out.println("##################");
		String expect = getResourceAsString("logdata.txt").trim();
		String diff = TestDataHelper.diff(expect, result);
		System.out.println(diff);
		assertTrue("log differs",diff.length()==0);
	}
	@Test
	public void testCSV() throws Exception {
		
		SessionService user = setupPerson("fred@example.com");
		ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(ctx);
		HashMap<String, Object> params=new HashMap<>();
		params.put("Message", "Hello world");
		Report report = new Report("SimpleTestReport.xml",params);
		setTransition(prov, ReportTemplateTransitionProvider.CSV, report);
		runTransition();
		
		checkRedirect("/Data/ServeData/1/SimpleTestReport.csv");
		SettableServeDataProducer prod = getContext().makeObjectWithDefault(
				SettableServeDataProducer.class, SessionDataProducer.class, ServeDataProducer.DEFAULT_SERVE_DATA_TAG);
		assertNotNull(prod);
		LinkedList<String> path = new LinkedList<>();
		path.add("1");
		path.add("SimpleTestReport.csv");
		MimeStreamData data = prod.getData(user, path);
		assertNotNull(data);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		data.write(out);
		String result = out.toString();
		System.out.println(result);
		assertEquals(getResourceAsString("SimpleTestReport.csv").replaceAll("\r\n", "\n"), result.replaceAll("\r\n", "\n"));
		
	}
}
