package uk.ac.ed.epcc.safe.accounting.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class Report {
	
	private ReportTemplate reportTemplate;
	private LinkedHashMap<String, Object> parameters;
	private Collection<String> contextParameters;
	private String extension;
	private String name;
	
	public Report(ReportTemplate template) {
		this(template, null);
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
		if (parameters != null) {
			this.parameters = new LinkedHashMap<String, Object>();
			this.parameters.putAll(parameters);
		}
		else {
			this.parameters = new LinkedHashMap<String, Object>();
		}
	}
	
	public Report(ReportTemplate reportTemplate,
			LinkedHashMap<String, Object> parameters,
			Collection<String> contextParameters) 
	{
		this(reportTemplate, parameters);
		setContextParameters(contextParameters);
	}

	public ReportTemplate getReportTemplate() {
		return reportTemplate;
	}
	
	public Map<String, Object> getParameters() {
		return new LinkedHashMap(parameters);
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
	
	public void setContextParameters(Collection<String> contextParameters) {
		this.contextParameters = new HashSet<String>(contextParameters);
	}
	
	public Collection<String> getContextParameters() {
		if (contextParameters == null) {
			return null;
		}
		return new HashSet<String>(contextParameters);
	}
}