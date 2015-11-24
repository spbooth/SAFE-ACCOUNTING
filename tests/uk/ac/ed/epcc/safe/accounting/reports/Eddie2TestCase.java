package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.SessionService;


public class Eddie2TestCase extends WebappTestBase {
	

	public static void setupParams(AppContext ctx, 
			Map<String,Object> params)
		throws ParserConfigurationException 
	{
		ReportType reportType = (ReportType)params.get("ReportType");
		if (reportType == null) {
		  params.put("ReportType", "html");
				
		}
		SessionService sessionService = ctx.getService(SessionService.class);
		sessionService.setCurrentPerson(1);
		AppUser user = sessionService.getCurrentPerson();		
		params.put("User", user);
		sessionService.setTempRole("SupportStaff");
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testProject() throws Exception {		
		
		ReportBuilder builder = new ReportBuilder(ctx,"ProjectUsage.xml","report.xsd");
		Map<String,Object> params=new HashMap<String,Object>();	
	    setupParams(ctx, params);
	    builder.setupExtensions(builder.getReportType("XML"),params);
	    params.put("StartDate", new Date(110,9,1));
	    params.put("EndDate", new Date(110,10,1));
	    params.put("Project","ecdf_physics");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		builder.renderXML(params, out);
		assertTrue(out.size()>0);
		//assertFalse(builder.hasErrors());
		System.out.println(out.toString());
		ReportBuilderTest.checkErrors(builder.getErrors());
		
	}

}
