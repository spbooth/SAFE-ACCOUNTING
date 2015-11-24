// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/** A SAX {@link ErrorHandler} that forwards the errors onto the {@link LoggerService} logging.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: XMLErrorHandler.java,v 1.2 2014/09/15 14:32:30 spb Exp $")


public class XMLErrorHandler implements ErrorHandler{
    private final AppContext conn;
    Logger log;
    public XMLErrorHandler(AppContext c){
    	conn=c;
    	log=c.getService(LoggerService.class).getLogger(getClass());
    }
	public void error(SAXParseException arg0) throws SAXException {
		log.error("Sax error "+arg0.getSystemId()+":"+arg0.getLineNumber()+":"+arg0.getColumnNumber()+" "+arg0.getMessage(),arg0);
		throw arg0;
	}

	public void fatalError(SAXParseException arg0) throws SAXException {
		log.error("Sax fatal error "+arg0.getSystemId()+":"+arg0.getLineNumber()+":"+arg0.getColumnNumber()+" "+arg0.getMessage(),arg0);
		throw arg0;
	}

	public void warning(SAXParseException arg0) throws SAXException {
		log.warn("Sax Warning "+arg0.getSystemId()+":"+arg0.getLineNumber()+":"+arg0.getColumnNumber()+" "+arg0.getMessage(), arg0);

	}


}