package uk.ac.ed.epcc.safe.accounting.reports.email;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;







import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.reports.ChartExtension;
import uk.ac.ed.epcc.safe.accounting.reports.CidChartExtension;
import uk.ac.ed.epcc.safe.accounting.reports.DeveloperReportType;
import uk.ac.ed.epcc.safe.accounting.reports.EmbeddedExtension;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayStreamData;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamData;
import uk.ac.ed.epcc.webapp.model.data.stream.MimeStreamDataWrapper;

/** A varient of {@link ReportBuilder} for creating template reports
 * that are delivered as email messages.
 * 
 * @author spb
 *
 */
public class MimeMessageReportBuilder extends ReportBuilder{
	// email embedded html. Don't change the mime type as some email clients 
	// only recognise text/html content.
	public static final ReportType	MHTML = new DeveloperReportType("MHTML","html","text/html","HTML email part");
	public static final ReportType	MTXT = new DeveloperReportType("MTXT","txt","text/plain","Text email part");
//  
	
	/* Having real trouble getting a mime structure that works in outlook
	 * 
	 * thunderbird manages this fine
	 * 
	 * 
	 *  multipart/mixed 
	 *    multipart/related
	 *     text/html & inkine
	 *     image/png & inline
	 *       
	 * 
	 *   mulipart/mixed
	 *     multipart/alternative
	 *       text/plain & inline
	 *       multipart/related
	 *          text/html & inline
	 *          image/png & inline
	 *    application/pdf & attachment        
	 *    
	 *     
	 *     Is the encolsing multipart/mixed essential
	 * 
	 */
	
	private final String report_template;
	private final Logger log;
	private final Map<String,Object> params;
	public MimeMessageReportBuilder(AppContext conn,String report_template,Map<String,Object> params) throws URISyntaxException, ParserConfigurationException, DataFault, InvalidArgument, TransformerFactoryConfigurationError, TransformerException {
		super(conn);
		
		log = getContext().getService(LoggerService.class).getLogger(getClass());
		this.report_template=report_template;
		setTemplate(report_template);
		if( params == null ){
			this.params = new HashMap<String, Object>();
		}else{
			this.params=params;
		}
		
	}

	
	
	
	/** create a {@link MimeBodyPart} containing a report of the specified type.
	 *
	 * @param type
	 * @return {@link MimeBodyPart}
	 * @throws Exception
	 */
	public MimeBodyPart makeReport(ReportType type, String disposition) throws Exception{
		setupExtensions(type, params);
			
			/* Be very careful modifying the mesage structure and test that it still diplays 
	         * as required. especially in outlook.
		 	*/
				ByteArrayStreamData data = new ByteArrayStreamData();
				renderXML(type, params, type.getResult(data.getOutputStream()));
				if( hasErrors()){
					Set<ErrorSet> errors = getErrors();
					for(ErrorSet s : errors){
						if( s.size() > 0){
							s.report(log);
						}
					}
					throw new ReportException("Error making report");
				}
			
				
				MimeBodyPart text = new MimeBodyPart();
				String filename = ReportBuilder.getTemplateName(report_template)+"."+type.getExtension();
				MimeStreamDataWrapper msd = new MimeStreamDataWrapper(data, type.getMimeType(), filename);
				text.setDataHandler(new DataHandler(msd));
				if( disposition != null ){
					text.setDisposition(disposition);
					if( Part.ATTACHMENT.equals(disposition)){
						// only want file-names for attachments don't want to trigger
						// unwanted attachment view in outlook
						text.setFileName(filename);
					}
				}
				ChartExtension ext = (ChartExtension) params.get("ChartExtension");
				if( ext instanceof CidChartExtension){
					Map<String,MimeStreamData> images = ((CidChartExtension)ext).getData();
					if( ! images.isEmpty()){

						MimeMultipart related= new MimeMultipart("related");
						related.addBodyPart(text);
						
						for(String name : images.keySet()){
							MimeBodyPart image = new MimeBodyPart();
							if( disposition != null){
								image.setDisposition(disposition);
							}
							MimeStreamData msd2 = images.get(name);
							image.setDataHandler(new DataHandler(new MimeStreamDataWrapper(msd2)));
							image.setContentID("<"+name+">");
							image.setFileName(msd2.getName());
							related.addBodyPart(image);
						}
						MimeBodyPart rep = new MimeBodyPart();
						rep.setContent(related);
						return rep;
					}
				}
				return text;
	}
				
				
		
	/** Make alternative versions of different types of report.
	 * 
	 * The client will select the "best" supported version to display.
	 * MS Outlook/Exchange is supposed cope with this as the message content but you only get to see one 
	 * version.  The types should be ordered from low to high fidelity.
	 * @param reportTypes
	 * @return {@link MimeMultipart}
	 * @throws MessagingException
	 * @throws Exception
	 */
	public MimeMultipart getAlternativeReport(String ... reportTypes ) throws MessagingException, Exception{
		// If we need to introduce a related multipart to contain images its apparently better
		// to make this the outer multipart. 
		MimeMultipart alternatives = new MimeMultipart("alternative");
		
		for( String type : reportTypes){
			ReportType reportType = getReportType(type);
			if( reportType == null ){
				log.error("Unknown report type "+type);
			}else{
				MimeBodyPart rep = makeReport(reportType,Part.INLINE);
				alternatives.addBodyPart(rep);
			}
		}
		return alternatives;
	}
	
	
	/** Add a series of reports as attachments to an existing  {@link MimeMultipart}
	 * 
	 * @param mp
	 * @param reportTypes
	 * @throws Exception
	 */
	public void addAttachments(MimeMultipart mp, String ... reportTypes) throws Exception{
		for( String type : reportTypes){
			ReportType reportType = getReportType(type);
			
			MimeBodyPart rep = makeReport(reportType,Part.ATTACHMENT);
			mp.addBodyPart(rep);
		}
	}
	
	
	public Object getFragment(String name, Object def){
		EmbeddedExtension ext = (EmbeddedExtension) params.get("Embedded");
		if( ext == null ){
			return def;
		}
		Object frag = ext.getFragment(name);
		if( frag == null ){
			return def;
		}
		return frag;
	}
	
	@Override
	protected Set<ReportType> getSpecialReportTypes() {
		Set<ReportType> s = super.getSpecialReportTypes();
		s.add(MHTML);
		s.add(MTXT);
		return s;
	}

}
