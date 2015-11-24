package uk.ac.ed.epcc.safe.accounting.reports;

import java.text.NumberFormat;

import uk.ac.ed.epcc.webapp.AppContext;

public final class CSVReportType extends ReportType {
	public CSVReportType(String name,String extension, String mime, String description) {
		super(name,extension, mime, description);
	}

	public NumberFormat getNumberFormat(AppContext conn){
		NumberFormat nf = super.getNumberFormat(conn);
		nf.setGroupingUsed(false);
		return nf;
	}
}