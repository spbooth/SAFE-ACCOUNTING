// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.reports.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimePart;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.content.TemplateFile;
import uk.ac.ed.epcc.webapp.email.Emailer;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

public class MimeMessageReportBuilderTest extends WebappTestBase {

	@Test
	public void testReportTypes() throws DataFault, URISyntaxException, ParserConfigurationException, InvalidArgument, TransformerFactoryConfigurationError, TransformerException{
		Map params = new HashMap();
		MimeMessageReportBuilder builder = new MimeMessageReportBuilder(ctx, "complex", params);
		
		assertEquals(builder.getReportTypeReg().getReportType("MHTML"),MimeMessageReportBuilder.MailReportTypeRegistry.MHTML);
		assertEquals(builder.getReportTypeReg().getReportType("MTXT"),MimeMessageReportBuilder.MailReportTypeRegistry.MTXT);
	}
	
	
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testSimpleRelated() throws MessagingException, Exception{
		Map params = new HashMap();
		MimeMessageReportBuilder builder = new MimeMessageReportBuilder(ctx, "complex", params);
		Emailer es = Emailer.getFactory(ctx);
		
		MimeMessage message = es.makeBlankEmail(ctx, new String[]{"fred@example.org"}, "Test report");
		MimeMultipart mp = new MimeMultipart("mixed");
//		MimeBodyPart text = new MimeBodyPart();
//		text.setText("Here is some text\nSome more text\n");
//		mp.addBodyPart(text);
		mp.addBodyPart(builder.makeReport(MimeMessageReportBuilder.MailReportTypeRegistry.MHTML, Part.INLINE));
		message.setContent(mp);
		
		
		message.saveChanges(); // This is needed to update headers
		
		checkRelatedMessage(message);
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testTextAndAttachment() throws MessagingException, Exception{
		Map params = new HashMap();
		MimeMessageReportBuilder builder = new MimeMessageReportBuilder(ctx, "complex", params);
		Emailer es = Emailer.getFactory(ctx);
		
		MimeMessage message = es.makeBlankEmail(ctx, new String[]{"fred@example.org"}, "Test report");
		MimeMultipart mp = new MimeMultipart("mixed");
		MimeBodyPart text = new MimeBodyPart();
		text.setText("Here is some text Some more text\n\n");
		//text.addHeader("format", "flowed");
		text.setDisposition(Part.INLINE);
		mp.addBodyPart(text);
		// Adding additional inline parts makes it work !!
		//mp.addBodyPart(builder.makeReport(MimeMessageReportBuilder.MHTML, Part.INLINE));
		builder.addAttachments(mp, "PDF");
		message.setContent(mp);
		
		
		message.saveChanges(); // This is needed to update headers
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		message.writeTo(stream);
		
	}
	
	@Test
	public void testThunderbirdStructure() throws MessagingException, IOException{
		Emailer es = Emailer.getFactory(ctx);
		MimeMessage m = new MimeMessage(es.getSession(ctx),getClass().getResourceAsStream("works.eml"));
		
		checkRelatedMessage(m);
	}
	
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testWithTemplate() throws TransformerFactoryConfigurationError, Exception{
		Emailer es = Emailer.getFactory(ctx);
		
		
		BufferedReader br = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream("test_email.txt")) );
	    StringBuffer text = new StringBuffer();
	    String line;
	    while((line = br.readLine()) != null ){
	        text.append( line );
	        text.append("\n");
	    }
		TemplateFile template = new TemplateFile(text.toString());
		MimeMessage m = es.templateMessage(new String[] { "fred@example.com"}, new Hashtable(), null, true,true, template);
		MimeMultipart mp = (MimeMultipart) m.getContent();
		
		Map params = new HashMap();
		MimeMessageReportBuilder builder = new MimeMessageReportBuilder(ctx, "complex", params);
		
		//mp.addBodyPart(builder.makeReport(MimeMessageReportBuilder.MHTML, Part.INLINE));

		builder.addAttachments(mp, "PDF");
		m.saveChanges();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
	
		m.writeTo(stream);
		
	}
	
	protected void checkRelatedMessage(MimeMessage m) throws IOException, MessagingException{
		
		MimeMultipart mp = (MimeMultipart) m.getContent();
		String toptype = mp.getContentType();
		//System.out.println("toptype="+toptype);
		assertTrue(toptype.contains("mixed"));
		
		mp = (MimeMultipart) mp.getBodyPart(0).getContent();
		
		checkRelated(mp);
	}



	protected void checkRelated(MimeMultipart mp) throws MessagingException {
		String innertype = mp.getContentType();
		//System.out.println("innertype="+innertype);
		assertTrue(innertype.contains("related"));
		
		assertTrue(mp.getCount()>1);
		MimeBodyPart part1 = (MimeBodyPart) mp.getBodyPart(0);
		String type1=part1.getContentType();
		String id1 = part1.getContentID();
		//System.out.println("part1 id="+id1);
		//System.out.println("part1 type="+type1);
		assertTrue(type1.startsWith("text/html"));
		assertNull(part1.getFileName());
		assertEquals(Part.INLINE, part1.getDisposition());
		for( int i=1 ; i < mp.getCount() ; i++){
			MimeBodyPart part2 = (MimeBodyPart) mp.getBodyPart(i);
			String type2=part2.getContentType();
			String id2 = part2.getContentID();
			//System.out.println("part"+i+" id="+id2);
			//System.out.println("part"+i+" type="+type2);
			assertNotNull(part2.getFileName());
			assertTrue(type2.startsWith("image/png"));
			assertEquals(Part.INLINE, part2.getDisposition());
		}
	}



	protected void checkAlternative(MimeMultipart mp) throws MessagingException {
		assertEquals(3,mp.getCount());
		assertTrue(mp.getContentType().contains("alternative"));
		MimePart txt = (MimePart) mp.getBodyPart(0);
		assertEquals(Part.INLINE,txt.getDisposition());
		assertTrue(txt.getContentType().startsWith("text/plain"));
		MimePart html = (MimePart) mp.getBodyPart(1);
		assertEquals(Part.INLINE,html.getDisposition());
		assertTrue(html.getContentType().startsWith("text/html"));
		MimePart pdf = (MimePart) mp.getBodyPart(2);
		assertEquals(Part.INLINE,pdf.getDisposition());
		assertTrue(pdf.getContentType().startsWith("application/pdf"));
	}
}