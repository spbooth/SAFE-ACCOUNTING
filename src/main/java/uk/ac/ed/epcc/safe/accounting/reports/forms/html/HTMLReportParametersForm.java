//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports.forms.html;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportTypeRegistry.ReportTypeInput;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.servlet.ServletService;



public class HTMLReportParametersForm {

	private final HTMLForm htmlForm;
	private final ReportBuilder reportBuilder;
	private Map<String,Object> params = null;
	
	public HTMLReportParametersForm(ReportBuilder builder, Map<String,Object> params,ReportType type) throws Exception{
		this.reportBuilder=builder;
		this.params = params;
		// initialise in constructor so we can trap exceptions in one place
		htmlForm = new HTMLForm( reportBuilder.getContext());
		if(reportBuilder.buildReportParametersForm(htmlForm, params)) {
			if( type == null ){
				// force a type parameter if we don't already have a type
				ReportTypeInput input = reportBuilder.getReportTypeReg().new ReportTypeInput();
				
				//			htmlForm.addInput(ReportBuilder.REPORT_TYPE_PARAM, "Display Format",input );
			}
		}
	}

	public HTMLForm getForm() {
		
		return htmlForm;
	}
	
	public Iterator<ReportType> getReportTypes() {
		ReportTypeInput input = reportBuilder.getReportTypeReg().new ReportTypeInput();
		return input.getItems();
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
		return ReportBuilder.extractReportParametersFromForm(form, conn.getService(ServletService.class).getParams());
		
	}

}