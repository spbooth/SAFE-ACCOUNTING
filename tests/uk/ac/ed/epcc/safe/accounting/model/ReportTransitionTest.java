package uk.ac.ed.epcc.safe.accounting.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.TestTimeService;
import uk.ac.ed.epcc.webapp.forms.inputs.CalendarFieldPeriodInput;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamData;
import uk.ac.ed.epcc.webapp.model.serv.ServeDataProducer;
import uk.ac.ed.epcc.webapp.model.serv.SettableServeDataProducer;
import uk.ac.ed.epcc.webapp.servlet.AbstractTransitionServletTest;
import uk.ac.ed.epcc.webapp.session.SessionDataProducer;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.CalendarFieldSplitPeriod;

public class ReportTransitionTest extends AbstractTransitionServletTest {

	public ReportTransitionTest() {
		// TODO Auto-generated constructor stub
	}

	
	@Test
	public void testDefaultTransition() throws Exception {
		TestTimeService serv = new TestTimeService();
		Calendar start = Calendar.getInstance();
		start.set(2018, Calendar.FEBRUARY, 10, 9, 0, 0);
		serv.setResult(start.getTime());
		ctx.setService(serv);
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
	public void testWithPeriod() throws Exception {
		TestTimeService serv = new TestTimeService();
		Calendar test_time = Calendar.getInstance();
		test_time.clear();
		test_time.set(2018, Calendar.APRIL, 23);
		serv.setResult(test_time.getTime());
		ctx.setService(serv);
		
		setupPerson("fred@example.com");
		ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(ctx);
		Report report = new Report("SimpleTestReportWithPeriod.xml");
		setTransition(prov, null, report);
		runTransition();
		checkForward("/scripts/transition.jsp");
		checkFormContent("/normalize.xsl", "initial_show_with_period.xml");
		setTransition(prov, ReportTemplateTransitionProvider.PREVIEW, report);
		addParam("Message", "hello world");
		setAction("Preview");
		runTransition();
		
		// This should redirect to PREVIEW of the report with the 
		// report parameters encoded in the context
		// reports are given RESTFUL urls with their parameters
		// so you can link to them this means we need to encode the
		// current parameters in the transition target
		
		// Need deined order as URL will depend on this
		LinkedHashMap<String, Object> params=new LinkedHashMap<>();
		
		
		// default period is the previous month
		Calendar cal = Calendar.getInstance();
		cal.setTime(test_time.getTime()); // start form test time
		cal.set(Calendar.MILLISECOND,0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.MONTH, -1);
		CalendarFieldSplitPeriod period = new CalendarFieldSplitPeriod(cal, Calendar.MONTH, 1, 1);
		CalendarFieldPeriodInput tmp = new CalendarFieldPeriodInput();
		tmp.setValue(period);
		tmp.setKey("Period");
		SetParamsVisitor vis = new SetParamsVisitor(true, params);
		tmp.accept(vis);
		
		params.put("Message", "hello world");
		Report report2 = new Report("SimpleTestReportWithPeriod.xml",params);
		checkRedirectToTransition(prov, ReportTemplateTransitionProvider.PREVIEW, report2);
		
		checkFormContent("/normalize.xsl", "test_preview_with_period.xml");
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
		String diff = TestDataHelper.diff(expect.replaceAll("\r\n", "\n"), result.replaceAll("\r\n", "\n"));
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
