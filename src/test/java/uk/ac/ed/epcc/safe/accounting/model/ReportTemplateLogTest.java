package uk.ac.ed.epcc.safe.accounting.model;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyFactoryTestCase;
import uk.ac.ed.epcc.safe.accounting.model.ReportTemplateLog.ReportLogFactory;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.exceptions.ParseException;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.AppUserFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;

public class ReportTemplateLogTest extends DataObjectPropertyFactoryTestCase<ReportLogFactory, ReportTemplateLog>
{

	public void setPerson() throws DataFault, ParseException {
		SessionService sess = getContext().getService(SessionService.class);
		AppUserFactory fac = sess.getLoginFactory();
		AppUser user = fac.makeFromString("fred@example.com");
		user.commit();
		sess.setCurrentPerson(user);
	}
	@Test
	public void testLogReport() throws DataFault, DataException, ParseException {
		setPerson();
		ReportLogFactory fac = getFactory();
		AppUser person = getContext().getService(SessionService.class).getCurrentPerson();
		assertNotNull(person);
		ReportTemplate expectedTarget = getTarget();
		List<String> expectedParameters = getParameters();
		fac.logReport(person, expectedTarget, expectedParameters);
		List<ReportTemplateLog> r = fac.all().toCollection(); 
		TestCase.assertEquals(1, r.size());
		ReportTemplateLog target = r.get(0);
		TestCase.assertEquals(expectedTarget, target.getTemplate());
		if( target.recordPerson()) {
			TestCase.assertEquals(person, target.getPerson());
		}
		String[] actualParameters = target.getParameters().split("/");
		TestCase.assertEquals(expectedParameters.size(), actualParameters.length);
		for (int i=0; i<actualParameters.length; i++) {
			TestCase.assertEquals(expectedParameters.get(i), actualParameters[i]);
		}
	}
	
	@Test
	public void testLogReportNoUser() throws DataFault, DataException {
		ReportLogFactory fac = getFactory();
		ReportTemplate expectedTarget = getTarget();
		List<String> expectedParameters = getParameters();
		fac.logReport(null, expectedTarget, expectedParameters);
		List<ReportTemplateLog> r = fac.all().toCollection(); 
		TestCase.assertEquals(1, r.size());
		ReportTemplateLog target = r.get(0);
		TestCase.assertEquals(expectedTarget, target.getTemplate());
		TestCase.assertNull(target.getPerson());
		String[] actualParameters = target.getParameters().split("/");
		TestCase.assertEquals(expectedParameters.size(), actualParameters.length);
		for (int i=0; i<actualParameters.length; i++) {
			TestCase.assertEquals(expectedParameters.get(i), actualParameters[i]);
		}
	}
	
	@Test
	public void testLogReportNoParameters() throws DataFault, DataException, ParseException {
		setPerson();
		ReportLogFactory fac = getFactory();
		ReportTemplate expectedTarget = getTarget();
		AppUser person = getContext().getService(SessionService.class).getCurrentPerson();
		assertNotNull(person);
		fac.logReport(person, expectedTarget, null);
		List<ReportTemplateLog> r = fac.all().toCollection(); 
		TestCase.assertEquals(1, r.size());
		ReportTemplateLog target = r.get(0);
		TestCase.assertEquals(expectedTarget, target.getTemplate());
		if( target.recordPerson()) {
			TestCase.assertEquals(person, target.getPerson());
		}
		TestCase.assertNull(target.getParameters());
	}
	
	@Test
	public void testLogReportEmptyParameters() throws DataFault, DataException, ParseException {
		setPerson();
		ReportLogFactory fac = getFactory();
		AppUser person = getContext().getService(SessionService.class).getCurrentPerson();
		assertNotNull(person);
		ReportTemplate expectedTarget = getTarget();
		List<String> expectedParameters = Collections.emptyList();
		fac.logReport(person, expectedTarget, expectedParameters);
		List<ReportTemplateLog> r = fac.all().toCollection(); 
		TestCase.assertEquals(1, r.size());
		ReportTemplateLog target = r.get(0);
		TestCase.assertEquals(expectedTarget, target.getTemplate());
		if( target.recordPerson()) {
			TestCase.assertEquals(person, target.getPerson());
		}
		TestCase.assertTrue(target.getParameters().isEmpty());
	}
	
	@Test
	public void testLogReportGetParametersList() throws DataFault, DataException, ParseException {
		setPerson();
		ReportLogFactory fac = getFactory();
		AppUser person = getContext().getService(SessionService.class).getCurrentPerson();
		assertNotNull(person);
		ReportTemplate expectedTarget = getTarget();
		List<String> expectedParameters = getParameters();
		fac.logReport(person, expectedTarget, expectedParameters);
		List<ReportTemplateLog> r = fac.all().toCollection(); 
		TestCase.assertEquals(1, r.size());
		ReportTemplateLog target = r.get(0);
		TestCase.assertEquals(expectedTarget, target.getTemplate());
		if( target.recordPerson()) {
			TestCase.assertEquals(person, target.getPerson());
		}
		TestCase.assertEquals(expectedParameters, target.getParametersList());
	}

	@Test
	public void testLogReportGetParametersListNull() throws DataFault, DataException, ParseException {
		setPerson();
		ReportLogFactory fac = getFactory();
		AppUser person = getContext().getService(SessionService.class).getCurrentPerson();
		assertNotNull(person);
		ReportTemplate expectedTarget = getTarget();
		fac.logReport(person, expectedTarget, null);
		List<ReportTemplateLog> r = fac.all().toCollection(); 
		TestCase.assertEquals(1, r.size());
		ReportTemplateLog target = r.get(0);
		TestCase.assertEquals(expectedTarget, target.getTemplate());
		if( target.recordPerson()) {
			TestCase.assertEquals(person, target.getPerson());
		}
		TestCase.assertNull(target.getParametersList());
	}
	
	@Test
	public void testLogReportGetParametersListEmpty() throws DataFault, DataException, ParseException {
		setPerson();
		ReportLogFactory fac = getFactory();
		AppUser person = getContext().getService(SessionService.class).getCurrentPerson();
		assertNotNull(person);
		ReportTemplate expectedTarget = getTarget();
		List<String> expectedParameters = new LinkedList<>();
		fac.logReport(person, expectedTarget, expectedParameters);
		List<ReportTemplateLog> r = fac.all().toCollection(); 
		TestCase.assertEquals(1, r.size());
		ReportTemplateLog target = r.get(0);
		TestCase.assertEquals(expectedTarget, target.getTemplate());
		if( target.recordPerson()) {
			TestCase.assertEquals(person, target.getPerson());
		}
		TestCase.assertEquals(expectedParameters, target.getParametersList());
	}
	
	private ReportTemplate getTarget() throws DataException {
		ReportTemplateFactory reportTemplateFactory = new ReportTemplateFactory(ctx);
		ReportTemplate template = reportTemplateFactory.findByFileName("BarTimeChart.xml");
		return template;
	}

	private List<String> getParameters() {
		List<String> parameters = new ArrayList<>();
		parameters.add("a:1");
		parameters.add("b:2");
		return parameters;
	}

	@Override
	public ReportLogFactory getFactory() {
		AppContext context = getContext();
		return new ReportTemplateLog.ReportLogFactory(
				context, 
				context.getService(SessionService.class).getLoginFactory(), 
				new ReportTemplateFactory<>(context));
	}

}
