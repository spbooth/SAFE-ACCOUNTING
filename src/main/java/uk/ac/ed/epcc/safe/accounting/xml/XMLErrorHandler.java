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
package uk.ac.ed.epcc.safe.accounting.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;


/** A SAX {@link ErrorHandler} that forwards the errors onto the {@link LoggerService} logging.
 * 
 * @author spb
 *
 */



public class XMLErrorHandler implements ErrorHandler{
    private final AppContext conn;
    Logger log;
    public XMLErrorHandler(AppContext c){
    	conn=c;
    	log=Logger.getLogger(c,getClass());
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