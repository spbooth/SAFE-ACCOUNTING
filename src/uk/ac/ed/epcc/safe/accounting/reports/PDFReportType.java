package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

public final class PDFReportType extends ReportType {
	public PDFReportType(String name,String extension, String mime, String description) {
		super(name,extension,mime,description);
	}

	public Result getResult(OutputStream out) throws Exception{
		FopFactory fopFactory = FopFactory.newInstance();
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

		return  new SAXResult(fop.getDefaultHandler());

	}
}