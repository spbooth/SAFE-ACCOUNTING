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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/** An {@link ErrorListener} that captures XSLT errors and forwards them to
 * the {@link AppContext} logging.
 * 
 * @author spb
 *
 */



public class TransformErrorListener implements ErrorListener {
    private final AppContext conn;
    private final Logger log;
    private final String name;
    public TransformErrorListener(AppContext c,String name){
    	conn=c;
    	log = conn.getService(LoggerService.class).getLogger(getClass());
    	if( name == null ){
    		this.name="unknown";
    	}else{
    		this.name=name;
    	}
    }
	public void error(TransformerException arg0) throws TransformerException {
		log.error("Error in transform "+name+" "+arg0.getMessageAndLocation(),arg0);
		throw arg0;
	}

	public void fatalError(TransformerException arg0)
			throws TransformerException {
		log.error("Fatal Error in transform "+name+" "+arg0.getMessageAndLocation(),arg0);
		throw arg0;

	}

	public void warning(TransformerException arg0) throws TransformerException {
		log.warn("Warning from transform "+name+" "+arg0.getMessageAndLocation(),arg0);
	}

}