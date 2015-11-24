package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.webapp.session.SessionService;

public class DeveloperReportType extends ReportType{

	public DeveloperReportType(String name, String extension, String mime,
			String description) {
		super(name, extension, mime, description);
	}

	@Override
	public boolean allowSelect(SessionService sess) {
		return sess.hasRole(ReportBuilder.REPORT_DEVELOPER);
	}
	
}