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
package uk.ac.ed.epcc.safe.accounting.reports;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;

import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.RestrictException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.GenericBinaryFilter;
import uk.ac.ed.epcc.webapp.model.NameFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.timer.TimerService;


/** A {@link ReportExtension} that handles access control to sections of a report.
 * 
 * @author spb
 *
 */



public class RestrictExtension extends ReportExtension {
	public static final String RESTRICT_LOC="http://safe.epcc.ed.ac.uk/restrict";
	public static final Feature REPORT_DEFAULT_ALLOW = new Feature("report.default_allow",true,"Default permission for reports is allow");
	public RestrictExtension(AppContext conn, ReportType type)
			throws ParserConfigurationException {
		super(conn, type);
	}

	/** Check the current user corresponds to the specified restrictions 
	 * @param node 
	 * @return boolean
	 */
	public  boolean checkAccess(Node node){
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			addError("Bad access control", "Non element fragment passed to checkAccess");
			return false;
		}
		AppContext conn = getContext();
		SessionService user = conn.getService(SessionService.class);
		if( user == null ){
			addError("Bad access control", "No current user");
			return false;
		}
//		if( user.hasRole(ReportBuilder.REPORT_DEVELOPER)){
//			return true;
//		}
		Element elem = (Element)node;
	
		NodeList roleNodes = elem.getElementsByTagNameNS(elem.getNamespaceURI(),"Sufficient");
		for(int i=0; i< roleNodes.getLength();i++){
			Element element = (Element) roleNodes.item(i);
			try{
				if( checkRole(element)){
					return true;
				}
			}catch(Exception e){
				addError("Bad Access","Error parsing Sufficient clause",e);
			}
		}
		roleNodes = elem.getElementsByTagNameNS(elem.getNamespaceURI(),"Required");
		
		for(int i=0; i< roleNodes.getLength();i++){
			Element element = (Element) roleNodes.item(i);
			try{
				if( ! checkRole(element)){
					return false;
				}
			}catch(Exception e){
				addError("Bad Access", "Error parsing Required clause",e);
				return false;
			}
		}
		
		return true;
	}
	private boolean checkRole(Element element) throws ReportException{
		AppContext conn = getContext();
		SessionService user = conn.getService(SessionService.class);
		if( user == null ){
			throw new RestrictException( "No current user");
		}
		String type = element.getAttribute("type");
		String val=getText(element);
		if( empty(type)){
			return user.hasRole(val);
		}else{
			return hasRelationship(conn, user, type, val);
		}
	}

	private boolean hasRelationship(AppContext conn, SessionService user, String type, String val) {
		DataObjectFactory fac = conn.makeObjectWithDefault(DataObjectFactory.class,null,type);
		if( fac != null){
			try {
				return fac.exists(conn.getService(SessionService.class).getRelationshipRoleFilter(fac, val));
				//return conn.getService(SessionService.class).hasRelationship(fac, null, val);
			} catch (Exception e) {
				addError("relationship_error", "Cannot check for relationship="+val+" on "+type, e);
				return false;
			}
		}
		addDeveloperError("no_role_selector", "No role selector for type "+type);
		return false;
	}
	@SuppressWarnings("unchecked")
	private boolean checkRelationship(Element element) throws ReportException, RestrictException{
		AppContext conn = getContext();
		SessionService user = conn.getService(SessionService.class);
		if( user == null ){
			throw new RestrictException("No current user");
		}
		String type = element.getAttribute("type");
		if( empty(type)) {
			throw new RestrictException("No type specified for relationship");
		}
		String role = element.getAttribute("role");
		if( empty(role) ) {
			throw new RestrictException("No role specified for relationship on "+type);
		}
		String name = getText(element);
		
			type=conn.getInitParameter("typealias."+type, type);

			DataObjectFactory fac = conn.makeObjectWithDefault(DataObjectFactory.class,null,type);
			if( fac == null) {
				throw new RestrictException("No factory found for type "+type);
			}
			if( empty(role)) {
				throw new RestrictException("No role specified for type "+type);
			}

			try {
				BaseFilter fil = conn.getService(SessionService.class).getRelationshipRoleFilter(fac, role);
				if( fil == null ){
					fil = new GenericBinaryFilter(fac.getTarget(),false);
				}
				if( empty(name) ){
					return fac.exists(fil);
				}else{
					// Want role on specific object
					if( fac instanceof NameFinder){
						return fac.matches(fil, (DataObject) ((NameFinder)fac).findFromString(name));
					}else{
						throw new RestrictException("factory "+type+" not a name finder but name="+name);
					}
				}
			}catch(RestrictException re) {
				throw re;
			} catch (Exception e) {
				throw new RestrictException( "Cannot check for relationship="+role+" on "+type, e);
			}

	}
	public boolean canUse(Document doc) throws ReportException{
		TimerService timer = getContext().getService(TimerService.class);
		if( timer != null ) {
			timer.startTimer("RestrictExtension.canUse");
		}
		try {
			NodeList roleNodes = doc.getElementsByTagNameNS(
					RESTRICT_LOC, "SufficientRole");
			boolean seen_rule=false;
			//log.debug("sufficient nodes "+roleNodes.getLength());
			for (int i = 0; i < roleNodes.getLength(); i++) {
				Element element = (Element) roleNodes.item(i);

				if( checkRole(element)){
					return true;
				}
			}
			roleNodes = doc.getElementsByTagNameNS(
					RESTRICT_LOC, "SufficientRelationship");
			//log.debug("sufficient nodes "+roleNodes.getLength());
			for (int i = 0; i < roleNodes.getLength(); i++) {
				Element element = (Element) roleNodes.item(i);

				if( checkRelationship(element)){
					return true;
				}

			}
			roleNodes = doc.getElementsByTagNameNS(RESTRICT_LOC,
					"RequireRole");
			//log.debug("require nodes "+roleNodes.getLength());
			for (int i = 0; i < roleNodes.getLength(); i++) {
				Element element = (Element) roleNodes.item(i);
				seen_rule=true;

				if( ! checkRole(element)){
					return false;
				}

			}
			roleNodes = doc.getElementsByTagNameNS(RESTRICT_LOC,
					"RequireRelationship");
			//log.debug("require nodes "+roleNodes.getLength());
			for (int i = 0; i < roleNodes.getLength(); i++) {
				Element element = (Element) roleNodes.item(i);
				seen_rule=true;

				if( ! checkRelationship(element)){
					return false;
				}

			}
			if( seen_rule ){
				// user has passed all explicitly required rules.
				return true;
			}
			//log.debug("default permission");
			return REPORT_DEFAULT_ALLOW.isEnabled(getContext());
		}finally {
			if( timer != null ) {
				timer.stopTimer("RestrictExtension.canUse");
			}
		}
	}

	@Override
	public boolean wantReplace(Element e) {
		return RESTRICT_LOC.equals(e.getNamespaceURI());
	}

	@Override
	public Node replace(Element e) {
		String name = e.getLocalName();
		switch(name) {
		case "RestrictedSection": return restrictedSection(e);
		case "RequriedRole":
		case "SufficientRole":
		case "RequireRelationship":
		case "SufficientRelationship":
			return null; // Just remove report access markup
		}
		
		return super.replace(e);
	}
	public Node restrictedSection(Element e) {
		boolean allow=true;
		NodeList children = e.getChildNodes();
		for(int i=0 ; i < children.getLength() ; i++) {
			Node n = children.item(i);
			if( n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals("Roles") && (n.getNamespaceURI() == null || n.getNamespaceURI().equals(RESTRICT_LOC))) {
				allow = allow && checkAccess(n);
			}
		}
		String expand = allow ? "Content" : "Fallback";
		return transformSubElementContents(e, expand);
	}
}