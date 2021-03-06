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

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.webapp.limits.LimitException;
import uk.ac.ed.epcc.webapp.logging.Logger;
/** An ErrorListener that forward the error to an ErrorSet
 * 
 * @author spb
 *
 */


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
		if( arg0.getCause() instanceof LimitException) {
			// abort processing;
			throw arg0;
		}
	}

	public void fatalError(TransformerException arg0)
			throws TransformerException {
		if( log != null ){
			log.error("Transform error "+tag, arg0);
		}
		if( set !=  null ){
			set.add("Transform fatal error "+tag, arg0.getMessage(), arg0);
		}
		throw arg0;
	}

	public void warning(TransformerException arg0) throws TransformerException {
		if( log != null ){
			log.warn("Transform warning "+tag, arg0);
		}
	}

}