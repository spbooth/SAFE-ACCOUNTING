package uk.ac.ed.epcc.safe.accounting.model;

import java.util.HashMap;
import java.util.Map;

import uk.ac.ed.epcc.webapp.forms.Form;

public class Report {
	
	private ReportTemplate reportTemplate;
	private Map<String, Object> parameters;
	private Form form;
	private String extension;
	private String name;
	
	public Report(ReportTemplate template) {
		this(template, new HashMap<String, Object>());
	}

	public Report(ReportTemplate template, Map<String, Object> parameters) {
		this.reportTemplate = template;
		if (reportTemplate != null) {
			String templateName = reportTemplate.getTemplateName();
			int i = templateName.indexOf(".");
			if (i >= 0) {
				this.name = templateName.substring(0, i);
			}
			else {
				this.name = templateName;
			}
		}
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
	
	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Report(template=" + getName() + ", parameters=" + getParameters() + ")";
	}
}
