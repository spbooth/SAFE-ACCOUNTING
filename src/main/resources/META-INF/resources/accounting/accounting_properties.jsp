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
<%@ page
	import="uk.ac.ed.epcc.webapp.*, 
	uk.ac.ed.epcc.webapp.model.*,
	uk.ac.ed.epcc.safe.accounting.*,
	uk.ac.ed.epcc.safe.accounting.properties.*"%>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://safe.epcc.ed.ac.uk/webapp" prefix="wb" %>
<wb:ServiceInit/>
<wb:session/>
<%
	String page_title = service_name+" Accounting Property Information";
    String producer = request.getParameter("producer");
    AccountingService accounting_service = conn.getService(AccountingService.class);
    if( accounting_service == null ){
%>
 <div class="block">
  <h2>No Accounting service</h2>
  <p>
  No accounting service is defined.
  </p>
  </div>
<%    	
    }else{
    UsageProducer usage_producer;
    if( producer == null ){
    	usage_producer = accounting_service.getUsageProducer();
    }else{
    	// Note to get an alternate Manager you need to do name:ALL
    	usage_producer = accounting_service.getUsageProducer(producer);
    }
%>
<%@ include file="std_header.jsf"%>
  <div class="block">
  <h1>Accounting Properties</h1>
  <P>Every accounting record consists of a set of properties. These properties can be generated 
  directly by parsing input data or they can be defined as an expression over other properties.
  </P>
  <p>Hover the mouse over the Name columns to see any additional information about the property,
  </p>
  </div>
<%
  if( usage_producer == null ){

%>
  <div class="block">
  <h2>No Usage Producer</h2>
  <p>
  No accounting tables are defined for the requested accounting set.
  </p>
  </div>
<% }else{ 
	  if( usage_producer instanceof UsageManager){
		  UsageManager<?> man = (UsageManager)usage_producer;
%>
	<div class="block">
	<h2>Sub-Producers</h2>
	<p>This is a composite producer made up from</p>
	<ul>
<% 
for(UsageProducer prod : man.getProducers(UsageProducer.class)){
%>
	<li><A href="accounting_properties.jsp?producer=<%=prod.getTag()%>"><%=prod.getTag() %></A></li>
<%
}

%>
	</ul>
	<p>Some properties may be defined in multiple tables (though may be implemented differently in each table). Others may only be defined in a sub-set of the tables.
	Operations that combine multiple records can only use properties defined in all the tables.
	</p>
	</div>
<%
	  }	
  
  MultiFinder multi = new MultiFinder();
  multi.addFinder(usage_producer.getFinder());
 
  
  for (FixedPropertyFinder knownRegistry : multi.getNested()) {
	%>
		
	<div class="block">
 	<h2><%=knownRegistry.toString()%> Properties</h2>
 
	<p><%=knownRegistry.getDescription()%></p>
	<% 
	Set<PropertyTag> properties = new HashSet<PropertyTag>();
	for( PropertyTag p : knownRegistry.getProperties()){
		if( usage_producer.hasProperty(p)){
			properties.add(p);
		}
	}
	if (properties.size() <= 0) { %>
	  	<p>No <%=knownRegistry.toString()%> Properties are defined for the requested accounting set.</p>
	<%} else {%>
 		<table>
 			<tr>				
				<th>Name</th>		
				<th>Type</th>		
				<th>Implementation</th>								
			</tr>	
			<%for (PropertyTag property : properties) {	
				
					String propertyName = property.getName();
					String propertyFullName = property.getFullName();
					String propertyDescription = property.getDescription();
					String propertyType = property.getTarget().getSimpleName();
					String propertyImplementationInfo = "";
					if( usage_producer instanceof PropertyImplementationProvider){
						propertyImplementationInfo =((PropertyImplementationProvider)usage_producer).getImplemenationInfo(property);
					}
				%>
				<tr>				
					<td title="<%=propertyDescription%>"><%=propertyFullName%></td>					
					<td><%=propertyType%></td>				
					<td><%=propertyImplementationInfo%></td>				
				</tr>		
			<%}%>
		</table>
	<%}%>
  	</div>
	  
 <%
 }
}
}%>

<%@ include file="std_footer.jsf"%>