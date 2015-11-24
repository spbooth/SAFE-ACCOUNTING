package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.OutputStream;
import java.text.NumberFormat;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.session.SessionService;

public class ReportType {
	public static final Feature REPORT_NUMBER_GROUP_FEATURE = new Feature("report.number_group", true, "Should the default number format use grouping");
	private final String name;
	private final String extension;
	private final String mime;
	final String description;

	public ReportType(String name,String extension, String mime,String description) {
		this.name = name;
		this.extension = extension;
		this.mime = mime;
		this.description=description;
		
	}
	public String name(){
		return name;
	}
	public String getExtension() {
		return extension;
	}

	public boolean allowSelect(SessionService sess){
		return true;
	}
	public String getMimeType() {
		return mime;
	}
	
	public NumberFormat getNumberFormat(AppContext conn){
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(REPORT_NUMBER_GROUP_FEATURE.isEnabled(conn)); // global off for now tests assume this
		nf.setMinimumFractionDigits(conn.getIntegerParameter("report.min.fractional", 0));
		nf.setMaximumFractionDigits(conn.getIntegerParameter("report.max.fractional", 3));
		return nf;
	}

	public Result getResult(OutputStream out) throws Exception{
		return new StreamResult(out);
	}
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof ReportType){
			return name.equals(((ReportType)obj).name);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	@Override
	public final String toString() {
		return name.toString();
	}
	
}