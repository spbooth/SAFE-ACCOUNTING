package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.model.Report;
import uk.ac.ed.epcc.safe.accounting.model.ReportTemplateTransitionProvider;
import uk.ac.ed.epcc.safe.accounting.reports.deferred.DeferredChartFactory;
import uk.ac.ed.epcc.safe.accounting.reports.deferred.DeferredChartFactory.DeferredChart;
import uk.ac.ed.epcc.webapp.charts.jfreechart.JFreeSetup;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamData;
import uk.ac.ed.epcc.webapp.servlet.AbstractTransitionServletTest;

public class ReportTransitionTest extends AbstractTransitionServletTest {

	public ReportTransitionTest() {
		
	}

	
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	@ConfigFixtures("serve_data.properties")
	public void testPreviewAddData() throws Exception {
		JFreeSetup.setup(ctx);
		takeBaseline();
		setupPerson("fred@example.com");
		setTime(2019, Calendar.OCTOBER, 1, 10, 00);
		
		ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(ctx);
		HashMap<String, Object> params=new HashMap<>();
		Report report = new Report("testChartsAddData.xml",params);
		setTransition(prov, null, report);
		runTransition();
		checkForward("/scripts/transition.jsp");
		checkFormContent("/normalize.xsl", "add_data_preview.xml");

	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	@ConfigFixtures({"serve_data.properties","deferred_image.properties"})
	public void testPreviewAddDataDeferred() throws Exception {
		JFreeSetup.setup(ctx);
		takeBaseline();
		setupPerson("fred@example.com");
		setTime(2019, Calendar.OCTOBER, 1, 10, 00);
		
		ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(ctx);
		HashMap<String, Object> params=new HashMap<>();
		Report report = new Report("testChartsAddData.xml",params);
		setTransition(prov, null, report);
		runTransition();
		checkForward("/scripts/transition.jsp");
		checkFormContent("/normalize.xsl", "add_data_deferred_preview.xml");
	
		DeferredChartFactory fac = new DeferredChartFactory(ctx);
		DeferredChart chart = fac.find(1);
		MimeStreamData spec = chart.getRawData();
		spec.write(System.out);
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		spec.write(s);
		checkContent(null, "deferred_spec.xml",s.toString());
		// line terminators require transform to remove data contents
		checkDiff("/cleanup.xsl", "add_data_deferred_report.xml");
	}
	
}
