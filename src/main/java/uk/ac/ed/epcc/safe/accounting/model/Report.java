package uk.ac.ed.epcc.safe.accounting.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class Report {
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Report other = (Report) obj;
		if (extension == null) {
			if (other.extension != null)
				return false;
		} else if (!extension.equals(other.extension))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}

	//private ReportTemplate reportTemplate;
	private LinkedHashMap<String, Object> parameters;
	private Collection<String> contextParameters;
	private String extension;
	private String name;
	private boolean preview=false;
	
	public Report(String template) {
		this(template, null);
	}

	public Report(String template, Map<String, Object> parameters) {
		if (template != null) {
			String templateName = template;
			int i = templateName.indexOf(".");
			if (i >= 0) {
				this.name = templateName.substring(0, i);
			}
			else {
				this.name = templateName;
			}
		}
		if (parameters != null) {
			this.parameters = new LinkedHashMap<>();
			this.parameters.putAll(parameters);
		}
		else {
			this.parameters = new LinkedHashMap<>();
		}
	}
	
	public Report(String reportTemplate,
			LinkedHashMap<String, Object> parameters,
			Collection<String> contextParameters) 
	{
		this(reportTemplate, parameters);
		setContextParameters(contextParameters);
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
		this.contextParameters = new HashSet<>(contextParameters);
	}
	
	public Collection<String> getContextParameters() {
		if (contextParameters == null) {
			return null;
		}
		return new HashSet<>(contextParameters);
	}

	/** Should a preview be forced
	 * 
	 * preview will also be shown for non-empty parameters
	 * 
	 * @return
	 */
	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}
}
