package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.reports.deferred.DeferredImageReportBuilder;
import uk.ac.ed.epcc.webapp.charts.jfreechart.JFreeSetup;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.mock.MockServletConfig;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamData;
import uk.ac.ed.epcc.webapp.servlet.ServeDataServlet;
import uk.ac.ed.epcc.webapp.servlet.ServletTest;

public class ServeImageTest extends ServletTest {
	
	@Before
	public void setConfig() throws ServletException{
		servlet=new ServeDataServlet();
		MockServletConfig config = new MockServletConfig(serv_ctx, "ServeDataServlet");
		servlet.init(config);
		req.servlet_path=ServeDataServlet.DATA_PATH;
	}
	@Test
	@DataBaseFixtures({"Eddie.xml","add_data_report.xml"})
	@ConfigFixtures("serve_data.properties")
	public void testGetImage() throws DataFault, ConsistencyError, DataException, IOException, ServletException {
		req.path_info="ServeData/1/image.png";
		setupPerson("fred@example.com");
		setTime(2019, Calendar.OCTOBER, 1, 10, 01);
		takeBaseline();
		doPost();
		
		assertEquals("image/png",res.getContentType());
		assertTrue(res.stream.isClosed());
		// No point checking this as its just the data stored in the DB fixture
		
	}
	
	public void checkPNG(byte data[]) {
		assertTrue(data.length > 8);
		assertEquals("PNG header[0]",(byte)0x89, data[0]);
		assertEquals("PNG header[1]",(byte)0x50, data[1]);
		assertEquals("PNG header[2]",(byte)0x4e, data[2]);
		assertEquals("PNG header[3]",(byte)0x47, data[3]);
		assertEquals("PNG header[4]",(byte)0x0d, data[4]);
		assertEquals("PNG header[5]",(byte)0x0a, data[5]);
		assertEquals("PNG header[6]",(byte)0x1a, data[6]);
		assertEquals("PNG header[7]",(byte)0x0a, data[7]);
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml","add_data_deferred_report.xml"})
	@ConfigFixtures({"serve_data.properties","deferred_image.properties"})
	public void testGetImageDeferred() throws Exception {
		JFreeSetup.setup(ctx);
		takeBaseline();
		req.path_info="DeferredCharts/1/image.png";
		setupPerson("fred@example.com");
		setTime(2019, Calendar.OCTOBER, 1, 10, 01);
		takeBaseline();
		doPost();
		
		assertEquals("image/png",res.getContentType());
		assertTrue(res.stream.isClosed());
		byte data[] = res.stream.getData();
		checkPNG(data);
		//writeFile("deferred_image.png",data);
		
		// We can't use the pre-calculated expected
		// Use a deferred image builder but with the original template
		DeferredImageReportBuilder builder = new DeferredImageReportBuilder(ctx, "testChartsAddData.xml");
		MimeStreamData msd = builder.makeImage();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		msd.write(out);
		byte exp[] = out.toByteArray();
		checkPNG(exp);
		assertEquals(exp.length, data.length);
		for(int i=0;i<exp.length;i++) {
			assertEquals("byte["+Integer.toString(i)+"]",exp[i],data[i]);
		}
		//assertEquals(238157928, data.hashCode());
		
		checkDiff("/cleanup.xsl","post_serv.xml");
	}
}
