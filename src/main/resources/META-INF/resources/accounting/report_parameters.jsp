<%--| Copyright - The University of Edinburgh 2015                             |--%>
<%--|                                                                          |--%>
<%--| Licensed under the Apache License, Version 2.0 (the "License");          |--%>
<%--| you may not use this file except in compliance with the License.         |--%>
<%--| You may obtain a copy of the License at                                  |--%>
<%--|                                                                          |--%>
<%--|    http://www.apache.org/licenses/LICENSE-2.0                            |--%>
<%--|                                                                          |--%>
<%--| Unless required by applicable law or agreed to in writing, software      |--%>
<%--| distributed under the License is distributed on an "AS IS" BASIS,        |--%>
<%--| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. |--%>
<%--| See the License for the specific language governing permissions and      |--%>
<%--| limitations under the License.                                           |--%>
<%@page import="uk.ac.ed.epcc.webapp.tags.WebappHeadTag"%>
<%@ page import="java.util.*"%>
<%@page import="uk.ac.ed.epcc.webapp.*"%>
<%@ page import="uk.ac.ed.epcc.webapp.model.*"%>
<%@ page import="uk.ac.ed.epcc.webapp.forms.html.*"%>
<%@ page import="uk.ac.ed.epcc.safe.accounting.reports.*"%>
<%@ page import="uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder.*"%>
<%@ page import="uk.ac.ed.epcc.safe.accounting.reports.forms.html.*"%>
<%@page import="uk.ac.ed.epcc.safe.accounting.servlet.ReportServlet"%>
<%@page import="uk.ac.ed.epcc.webapp.servlet.*"%>
<%@ taglib uri="http://safe.epcc.ed.ac.uk/webapp" prefix="wb" %>
<wb:ServiceInit/>
<wb:session/>
<%
	Map<String, Object> params = conn.getService(ServletService.class).getParams();
	String templateName = (String) params.get("TemplateName");
	ReportType reportType = null;
	Object type = params.get("ReportType");
	
	ReportBuilder builder;
	HTMLReportParametersForm form;
	String templateFileName;
	try {
		builder = ReportBuilder.getInstance(conn);
		if (type != null) {
			if (type instanceof ReportType) {
				reportType = (ReportType) type;
			} else {
				reportType = builder.getReportTypeReg().getReportType(type.toString());
			}
		}

		templateFileName = templateName
				+ (reportType == null ? "" : "."
						+ reportType.getExtension());
		ReportBuilder.setTemplate(conn, builder, templateName);
		
		form = new HTMLReportParametersForm(builder, params, reportType);
	} catch (Exception e) {
		conn.error(e, "Error making parameter form");
		if (session_service.hasRole(ReportBuilder.REPORT_DEVELOPER)) {
%>
<jsp:forward page="/messages.jsp">
<jsp:param name="message_type" value="bad_report_parameters"/>
<jsp:param name="message_extra" value="<%=e.getMessage() %>"/>
</jsp:forward>
<%
	} else {
%>
<jsp:forward page="/messages.jsp">
<jsp:param name="message_type" value="bad_report_parameters"/>
</jsp:forward>
<%
	}
		return;
	}
	String page_title = builder.getTitle();
%>
<wb:css url="service_desk.css"/>
<%@ include file="std_header.jsf"%>

<div class="block">
<h1><%=page_title%></h1>
<%=builder.addParameterText(new HtmlBuilder()).toString()%>

<h2>This page allows you to set the parameters for your report.</h2>
<p>
Fields shown in black are required fields. Fields shown in grey are optional and
may be omitted.
</p>
</div>
<wb:FormContext/>
<div class="block">
<h2>Please set the following parameters:</h2>

<form method="get" 
   action="<%=response.encodeURL(web_path + "/ReportServlet/"
					+ templateFileName)%>">
  <input type="hidden" name="action" value="GENERATE_REPORT">
  <%=form.getFormAsHTML(request)%>
  <div class="action_buttons">

  <%
	Iterator<ReportType> items = form.getReportTypes();
	while(items.hasNext()) {
		ReportType rep = items.next();
		%>
		<input type="submit" name="submit" value="Export as <%=rep.toString()%>">
	<%}%>
	
  <input type="submit" name="submit" value="Preview Report">
  </div>
  </form>
</div>

<%@ include file="std_footer.jsf" %>