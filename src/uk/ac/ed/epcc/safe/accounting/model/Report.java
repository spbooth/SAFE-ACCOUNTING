package uk.ac.ed.epcc.safe.accounting.model;

import java.util.HashMap;
import java.util.Map;

import uk.ac.ed.epcc.webapp.forms.Form;

public class Report {
	
	private ReportTemplate reportTemplate;
	private Map<String, Object> parameters;
	private Form form;
	
	public Report(ReportTemplate template) {
		this(template, new HashMap<String, Object>());
	}

	public Report(ReportTemplate template, Map<String, Object> parameters) {
		System.out.println("CREATING REPORT, parameters=" + parameters);
		this.reportTemplate = template;
		this.parameters = parameters;
	}
	
	public Report(ReportTemplate template, Form form) {
		this.reportTemplate = template;
		this.form = form;
	}

	public ReportTemplate getReportTemplate() {
		return reportTemplate;
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public Form getForm() {
		return form;
	}

}
