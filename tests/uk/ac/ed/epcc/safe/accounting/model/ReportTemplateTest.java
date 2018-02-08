// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.content.Button;
import uk.ac.ed.epcc.webapp.content.HtmlBuilder;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.content.TableXMLFormatter;
import uk.ac.ed.epcc.webapp.forms.registry.FormOperations;
import uk.ac.ed.epcc.webapp.forms.result.ChainedTransitionResult;
import uk.ac.ed.epcc.webapp.forms.transition.TransitionFactory;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.servlet.TransitionServlet;


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
	
	@Test
	@DataBaseFixtures({"ReportTemplateData.xml"})
	public void testIndexTable() throws DataFault, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		ReportTemplateFactory reportTemplateFactory = ctx.makeObject(ReportTemplateFactory.class,"ReportTemplate");
		Table<String,ReportTemplate> t= reportTemplateFactory.getIndexTable();
		HtmlBuilder builder = new HtmlBuilder();
		
//		TransitionFactory provider = TransitionServlet.getProviderFromName(ctx,"Reports:ReportTemplate");
//		assertNotNull(provider);
//		for(ReportTemplate r : t.getRows()){
//			if( provider.allowTransition(ctx, r, FormOperations.Edit)){
//				t.put("Edit", r, new Button(ctx, "Edit",new ChainedTransitionResult(provider,r,FormOperations.Edit)));
//			}
//		}
		
	  	TableXMLFormatter<String,ReportTemplate> fmt = new TableXMLFormatter<String,ReportTemplate>(builder, null);
	  	fmt.add(t);
	  	checkContent("/normalize.xsl", "report_index.xml", builder.toString());
		
	}
}