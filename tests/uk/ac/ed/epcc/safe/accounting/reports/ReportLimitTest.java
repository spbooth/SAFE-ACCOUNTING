package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.model.Report;
import uk.ac.ed.epcc.safe.accounting.model.ReportTemplateTransitionProvider;
import uk.ac.ed.epcc.safe.accounting.model.SetParamsVisitor;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.CalendarFieldPeriodInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TypeError;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.servlet.AbstractTransitionServletTest;
import uk.ac.ed.epcc.webapp.time.CalendarFieldSplitPeriod;

public class ReportLimitTest extends AbstractTransitionServletTest {

	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testTimeChart() throws Exception{
		// TEst without limit
		runTimechart();
		
		checkFormContent("/normalize.xsl", "preview_content.xml");
	
	}

	@Test
	@DataBaseFixtures({"Eddie.xml"})
	@ConfigFixtures("limit.properties")
	public void testTimeChartLimit() throws Exception{
		ctx.setService(new CountingLimitService(ctx, 5));
		// TEst without limit
		runTimechart();
		
		checkFormContent("/normalize.xsl", "limit_content.xml");
	
	}
	/**
	 * @throws DataException
	 * @throws TypeError
	 * @throws Exception
	 * @throws TransitionException
	 * @throws ServletException
	 * @throws IOException
	 */
	private void runTimechart()
			throws DataException, TypeError, Exception, TransitionException, ServletException, IOException {
		setupPerson("spb@epcc.ed.ac.uk");
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0L);
		c.set(Calendar.YEAR, 2008);
		c.set(Calendar.MONTH,Calendar.SEPTEMBER);
		c.set(Calendar.DAY_OF_MONTH,26);
		c.set(Calendar.HOUR_OF_DAY,0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND,0);
		c.set(Calendar.MILLISECOND,0);
		CalendarFieldSplitPeriod p = new CalendarFieldSplitPeriod(c,Calendar.MONTH,1,1);
		ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(ctx);
		
		
		CalendarFieldPeriodInput input = new CalendarFieldPeriodInput();
		input.setKey("Period");
		input.setValue(p);
		LinkedHashMap params = new LinkedHashMap<>();
		params.put("Plot", "Wall");
		params.put("Group","UserName");
		SetParamsVisitor vis = new SetParamsVisitor(true, params);
		input.accept(vis);
		
		Report report = new Report("TimeChart.xml");
		setTransition(prov, ReportTemplateTransitionProvider.PREVIEW, report);
		addParam("Plot","Wall");
		addParam("Group","UserName");
		addParam(input);
		setAction("Preview");
		runTransition();
		checkRedirectToTransition(prov, ReportTemplateTransitionProvider.PREVIEW, new Report("TimeChart.xml",params));
	}
}
