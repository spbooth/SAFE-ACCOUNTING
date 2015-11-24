// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import java.text.NumberFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.model.ClassificationFactory;
import uk.ac.ed.epcc.webapp.model.NameFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.forms.RoleSelector;
import uk.ac.ed.epcc.webapp.model.relationship.Relationship;
import uk.ac.ed.epcc.webapp.model.relationship.RelationshipProvider;
import uk.ac.ed.epcc.webapp.session.SessionService;


/** A {@link ReportExtension} that handles access control to sections of a report.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: RestrictExtension.java,v 1.11 2015/08/13 21:52:34 spb Exp $")


public class RestrictExtension extends ReportExtension {
	public static final String RESTRICT_LOC="http://safe.epcc.ed.ac.uk/restrict";
	public static final Feature REPORT_DEFAULT_ALLOW = new Feature("report.default_allow",true,"Default permission for reports is allow");
	public RestrictExtension(AppContext conn, NumberFormat nf)
			throws ParserConfigurationException {
		super(conn, nf);
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
			addError("Bad access control", "No current user");
			return false;
		}
		String type = element.getAttribute("type");
		String val=getText(element);
		if( type == null || type.length()==0){
			return user.hasRole(val);
		}else{
			RoleSelector<?> sel = conn.makeObject(RoleSelector.class,type);
			if( sel != null){
				return sel.hasRole(val, user);
			}else{
				addDeveloperError("no_role_selector", "No role selector for type "+type);
				return false;
			}
		}
	}
	@SuppressWarnings("unchecked")
	private boolean checkRelationship(Element element) throws ReportException{
		AppContext conn = getContext();
		SessionService user = conn.getService(SessionService.class);
		if( user == null ){
			addError("Bad access control", "No current user");
			return false;
		}
		String type = element.getAttribute("type");
		String role = element.getAttribute("role");
		String name = getText(element);
		RoleSelector rel = conn.makeObjectWithDefault(RoleSelector.class, Relationship.class,type);
		if( rel != null){
			if( name == null || name.trim().length()==0 ){
				return rel.hasRole(role, user);
			}else{
				// Want role on specific object
				DataObjectFactory fac = rel.getTargetFactory();
				if( fac instanceof NameFinder){
					return rel.hasRole(user, (DataObject) ((NameFinder)fac).findFromString(name), role);
				}else{
					addDeveloperError("not_name_finder", type);
				}
			}
		}else{
			addDeveloperError("no_relationship", type);
		}
		return false;
	}
	public boolean canUse(Document doc){
		NodeList roleNodes = doc.getElementsByTagNameNS(
				RESTRICT_LOC, "SufficientRole");
		boolean seen_rule=false;
		//log.debug("sufficient nodes "+roleNodes.getLength());
		for (int i = 0; i < roleNodes.getLength(); i++) {
			Element element = (Element) roleNodes.item(i);
			try {
				if( checkRole(element)){
					return true;
				}
			} catch (ReportException e) {
				addError("role_error", "Error checking sufficient role",e);
			}
		}
		roleNodes = doc.getElementsByTagNameNS(
				RESTRICT_LOC, "SufficientRelationship");
		//log.debug("sufficient nodes "+roleNodes.getLength());
		for (int i = 0; i < roleNodes.getLength(); i++) {
			Element element = (Element) roleNodes.item(i);
			try {
				if( checkRelationship(element)){
					return true;
				}
			} catch (ReportException e) {
				addError("relation_error", "Error checking sufficient relationship",e);
			}
		}
		roleNodes = doc.getElementsByTagNameNS(RESTRICT_LOC,
				"RequireRole");
		//log.debug("require nodes "+roleNodes.getLength());
		for (int i = 0; i < roleNodes.getLength(); i++) {
			Element element = (Element) roleNodes.item(i);
			seen_rule=true;
			try {
				if( ! checkRole(element)){
					return false;
				}
			} catch (ReportException e) {
				addError("role_error","Error checking required role",e);
				return false;
			}
		}
		roleNodes = doc.getElementsByTagNameNS(RESTRICT_LOC,
				"RequireRelationship");
		//log.debug("require nodes "+roleNodes.getLength());
		for (int i = 0; i < roleNodes.getLength(); i++) {
			Element element = (Element) roleNodes.item(i);
			seen_rule=true;
			try {
				if( ! checkRelationship(element)){
					return false;
				}
			} catch (ReportException e) {
				addError("role_error","Error checking required role",e);
				return false;
			}
		}
		if( seen_rule ){
			// user has passed all explicitly required rules.
			return true;
		}
		//log.debug("default permission");
		return REPORT_DEFAULT_ALLOW.isEnabled(getContext());
	}
}