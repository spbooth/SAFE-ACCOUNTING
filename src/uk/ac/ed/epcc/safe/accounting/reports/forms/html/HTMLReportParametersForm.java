// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports.forms.html;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder.ReportTypeInput;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.servlet.ServletService;
@uk.ac.ed.epcc.webapp.Version("$Id: HTMLReportParametersForm.java,v 1.23 2014/09/15 14:32:29 spb Exp $")


public class HTMLReportParametersForm {

	private final HTMLForm htmlForm;
	private final ReportBuilder reportBuilder;
	private Map<String,Object> params = null;
	
	public HTMLReportParametersForm(ReportBuilder builder, Map<String,Object> params,ReportType type) throws Exception{
		this.reportBuilder=builder;
		this.params = params;
		// initialise in constructor so we can trap exceptions in one place
		htmlForm = new HTMLForm( reportBuilder.getContext());
		reportBuilder.buildReportParametersForm(htmlForm, params);
		if( type == null ){
			// force a type parameter if we don't already have a type
			ReportTypeInput input = reportBuilder.new ReportTypeInput();
			input.setOptional(false);
			htmlForm.addInput(ReportBuilder.REPORT_TYPE_PARAM, "Display Format",input );
		}
	}

	public HTMLForm getForm() {
		
		return htmlForm;
	}
	
	public final String getFormAsHTML(HttpServletRequest req) {
		
			HTMLForm htmlForm = getForm();
			if (htmlForm == null) {
				return "";
			} else {
				return htmlForm.getHtmlFieldTable(req)
					+ "\n" + htmlForm.getActionButtons();
			}
			
		
	}

	public boolean parseForm(HttpServletRequest req) 
		throws DataException 
	{
		AppContext conn = reportBuilder.getContext();
		
		
		HTMLForm form = getForm();
		
		if (form == null) {
			return false;
		}
		boolean ok = form.parsePost(req);
		if (!ok) {
			conn.getService(LoggerService.class).getLogger(getClass()).debug("form failed to parse");
			return false;
		}
		
		// next we ...
		return reportBuilder.parseReportParametersForm(form, conn.getService(ServletService.class).getParams());
		
	}

}