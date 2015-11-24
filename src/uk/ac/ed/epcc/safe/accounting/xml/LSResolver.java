// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.xml;

import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay.TextFile;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
/** class to implement schema sheet resolution via TextFileOverlay
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: LSResolver.java,v 1.3 2014/09/15 14:32:30 spb Exp $")

public class LSResolver implements LSResourceResolver{
    DOMImplementationLS ls_imp;
    private final LSResourceResolver nested;
    private final TextFileOverlay schema_overlay;
    private final String group;
    Logger log;
	public LSResolver(DOMImplementation imp,TextFileOverlay overlay, String group,LSResourceResolver parent) throws ParserConfigurationException{
		this.schema_overlay=overlay;
		this.group=group;
		ls_imp = (DOMImplementationLS) imp.getFeature("LS", "3.0");
		nested = parent;
		log=overlay.getContext().getService(LoggerService.class).getLogger(getClass());
	}
	public LSInput resolveResource(String type, String namespaceURI, String publicId,
			String systemId, String baseURI) {
		log.debug("resolveResource called "+type+" "+namespaceURI+" "+publicId+" "+systemId+" "+baseURI);
		if( schema_overlay != null && schema_overlay.isValid()){
			URL base = schema_overlay.getBaseURL();
			log.debug("base url is "+base);
			// schema relative to unknown or our base url
			if( type.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI) && 
					(baseURI==null || (base!=null && baseURI.startsWith(base.toString())))){
				log.debug("go for overlay lookup");
				String name=systemId;
				if( name.contains("/")){
					name=name.substring(name.lastIndexOf("/"));
				}
				LSInput input = ls_imp.createLSInput();
				TextFile sheet=null;
				try {
					sheet = schema_overlay.find(group, name);
				} catch (DataFault e) {
					schema_overlay.getContext().error(e, "Error getting resource");
				}
				if (sheet != null && sheet.hasData()) {
				
						input.setStringData(sheet.getData());
						return input;
					
				}
			}
		}
		log.debug("No schema found for "+type+" "+namespaceURI+" "+publicId+" "+systemId);
		if( nested != null ){
			return nested.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
		}
		return null;
	}
	
}