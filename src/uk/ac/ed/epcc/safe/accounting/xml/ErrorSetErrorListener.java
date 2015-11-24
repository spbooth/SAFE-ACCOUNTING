// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.xml;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.webapp.logging.Logger;
/** An ErrorListener that forward the error to an ErrorSet
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ErrorSetErrorListener.java,v 1.4 2014/09/15 14:32:30 spb Exp $")

public class ErrorSetErrorListener implements ErrorListener {
	private final Logger log;
	private final ErrorSet set;
	private final String tag;
	public ErrorSetErrorListener(String tag,Logger log, ErrorSet set){
		this.tag=tag;
		this.log=log;
		this.set=set;
	}
	public void error(TransformerException arg0) throws TransformerException {
		
		if( log != null ){
			log.warn("Transform error "+tag, arg0);
		}
		if( set != null ){
			set.add("Transform error "+tag, arg0.getMessage(), arg0);
		}
	}

	public void fatalError(TransformerException arg0)
			throws TransformerException {
		if( log != null ){
			log.warn("Transform error "+tag, arg0);
		}
		if( set !=  null ){
			set.add("Transform fatal error "+tag, arg0.getMessage(), arg0);
		}
	}

	public void warning(TransformerException arg0) throws TransformerException {
		if( log != null ){
			log.warn("Transform warning "+tag, arg0);
		}
	}

}