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
package uk.ac.ed.epcc.safe.accounting.upload;

import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.db.AccountingClassificationFactory;
import uk.ac.ed.epcc.safe.accounting.db.ClassificationParseTarget;
import uk.ac.ed.epcc.safe.accounting.db.ClassificationUpdater;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
/** UploadParser to populate an {@link AccountingClassificationFactory} 
 * 
 * @author spb
 *
 */


public class ClassificationUploadParser implements UploadParser, Contexed {
    private final AppContext conn;
    private final String mode;
    public ClassificationUploadParser(AppContext c, String mode){
    	conn=c;
    	this.mode=mode;
    }
	@SuppressWarnings("unchecked")
	public String upload(Map<String, Object> parameters) throws UploadException {
		String update = (String) parameters.get("update");
		if( update == null ){
			throw new UploadException("No update data");
		}
		String target_tag = conn.getInitParameter(mode+".target");
		if( target_tag == null){
			throw new UploadException("No target specified for "+mode);
		}
		ClassificationParseTarget target = conn.makeObject(ClassificationParseTarget.class, target_tag);
		if( target == null ){
			throw new UploadException("Invalid Target");
		}
		ClassificationUpdater updater = new ClassificationUpdater(conn, target);
		return updater.receiveData(parameters, update);
	}
	public AppContext getContext() {
		return conn;
	}

}