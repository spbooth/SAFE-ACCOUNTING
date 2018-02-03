package uk.ac.ed.epcc.safe.accounting.model;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.servlet.AbstractTransitionServletTest;

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
}
