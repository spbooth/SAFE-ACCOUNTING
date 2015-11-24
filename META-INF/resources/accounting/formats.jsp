<%@page import="uk.ac.ed.epcc.safe.accounting.reports.ParameterExtension"%>
<%@page import="uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter"%>
<%
String page_title="Reporting formats";
%>
<%@ include file="/session.jsf"%>
<%@ include file="/std_header.jsf"%>
<div class="block">
<% HtmlBuilder cb = new HtmlBuilder();
ParameterExtension.getDocumentation(conn, cb);
%>
<%=cb.toString() %>
</div>
<%@ include file="/std_footer.jsf"%>
