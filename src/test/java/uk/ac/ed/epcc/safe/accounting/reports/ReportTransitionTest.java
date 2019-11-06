package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.model.Report;
import uk.ac.ed.epcc.safe.accounting.model.ReportTemplateTransitionProvider;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.TestTimeService;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.html.BaseHTMLForm;
import uk.ac.ed.epcc.webapp.forms.inputs.CalendarFieldPeriodInput;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.ClassificationFactory;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamData;
import uk.ac.ed.epcc.webapp.model.serv.ServeDataProducer;
import uk.ac.ed.epcc.webapp.model.serv.SettableServeDataProducer;
import uk.ac.ed.epcc.webapp.servlet.AbstractTransitionServletTest;
import uk.ac.ed.epcc.webapp.session.SessionDataProducer;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.CalendarFieldSplitPeriod;
import uk.ac.ed.epcc.webapp.timer.NullTimerService;

public class ReportTransitionTest extends AbstractTransitionServletTest {

	public ReportTransitionTest() {
		// TODO Auto-generated constructor stub
	}

	
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	@ConfigFixtures("serve_data.properties")
	public void testPreviewAddData() throws Exception {
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
		checkDiff("/cleanup.xsl", "add_data_report.xml");
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	@ConfigFixtures({"serve_data.properties","deferred_image.properties"})
	public void testPreviewAddDataDeferred() throws Exception {
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
		checkDiff("/cleanup.xsl", "add_data_deferred_report.xml");
	}
	
}
