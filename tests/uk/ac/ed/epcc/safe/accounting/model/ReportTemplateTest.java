package uk.ac.ed.epcc.safe.accounting.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;


public class ReportTemplateTest extends WebappTestBase{
	
	


	@Test
	@DataBaseFixtures({"ReportTemplateData.xml"})
	public void testReportTemplatesList() throws Exception{
		ReportTemplateFactory reportTemplateFactory = new ReportTemplateFactory(ctx);
		List<ReportTemplate> reportTemplates = reportTemplateFactory.all().toCollection();		
		assertTrue(reportTemplates.size() > 0);

		assertNotNull(reportTemplateFactory.findByFileName("AggregateTimeChart.xml"));
		@SuppressWarnings("unused")
		ReportBuilder reportBuilder;
		
		//= new ReportBuilder(ctx,"AggregateTimeChart","report.xsd");	
		
		assertNotNull(reportTemplateFactory.findByFileName("BarTimeChart.xml"));
		reportBuilder = new ReportBuilder(ctx,"BarTimeChart","report.xsd");	
		
		assertNotNull(reportTemplateFactory.findByFileName("PieTimeChart.xml"));
		reportBuilder = new ReportBuilder(ctx,"PieTimeChart","report.xsd");	
		
		//assertNotNull(reportTemplateFactory.findByFileName("ProjectJobs.xml"));
		//reportBuilder = new ReportBuilder(ctx,"ProjectJobs","report.xsd");	

		//assertNotNull(reportTemplateFactory.findByFileName("ProjectUsage.xml"));
		//reportBuilder = new ReportBuilder(ctx,"ProjectUsage","report.xsd");	
		
		assertNotNull(reportTemplateFactory.findByFileName("ServiceUsage.xml"));	
		reportBuilder = new ReportBuilder(ctx,"ServiceUsage","report.xsd");	
	
		assertNotNull(reportTemplateFactory.findByFileName("TimeChart.xml"));
		reportBuilder = new ReportBuilder(ctx,"TimeChart","report.xsd");	
		
		assertNotNull(reportTemplateFactory.findByFileName("UserUsage.xml"));
		reportBuilder = new ReportBuilder(ctx,"UserUsage","report.xsd");	
		
		assertNotNull(reportTemplateFactory.findByFileName("UserJobs.xml"));
		reportBuilder = new ReportBuilder(ctx,"UserJobs","report.xsd");	
		
	}
}
