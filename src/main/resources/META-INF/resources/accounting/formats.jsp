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
<%@page import="uk.ac.ed.epcc.safe.accounting.reports.ParameterExtension"%>
<%@page import="uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter"%>
<%@page import="uk.ac.ed.epcc.webapp.content.HtmlBuilder" %>
<%
String page_title="Reporting formats";
%>
<%@ taglib uri="http://safe.epcc.ed.ac.uk/webapp" prefix="wb" %>
<wb:ServiceInit/>
<wb:session/>
<%@ include file="std_header.jsf"%>
<div class="block">
<% HtmlBuilder cb = new HtmlBuilder();
ParameterExtension.getDocumentation(conn, cb);
%>
<%=cb.toString() %>
</div>
<%@ include file="std_footer.jsf"%>