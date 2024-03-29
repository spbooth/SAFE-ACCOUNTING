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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.db.UploadParseTarget;
import uk.ac.ed.epcc.safe.accounting.db.UploadParseTargetUpdater;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.stream.StreamData;
import uk.ac.ed.epcc.webapp.servlet.ServletService;
/** UploadParser to populate an {@link UploadParseTarget} 
 * 
 * The target factory can be hard-wired by setting the parameter
 * <b><i>tag</i>.target</b> (Where tag is the construction tag for the {@link UploadParser} itself.
 * Otherwise the target table is taken from the <b>table</b> value in the map passed to the
 * {@link #upload(Map)} method.
 * 
 * @author spb
 *
 */


public class UploadParseTargetUploadParser extends AbstractContexed implements UploadParser{
    private final String mode;
    public UploadParseTargetUploadParser(AppContext c, String mode){
    	super(c);
    	this.mode=mode;
    }
	@SuppressWarnings("unchecked")
	public String upload(Map<String, Object> parameters) throws UploadException {
		Object o = parameters.get(ServletService.DEFAULT_PAYLOAD_PARAM);
		InputStream update = null;
		if( o != null) {
			if( o instanceof InputStream) {
				update = (InputStream) o;
			}else if( o instanceof String ) {
				update = new ByteArrayInputStream(((String)o).getBytes());
			}else if( o instanceof StreamData) {
				update = ((StreamData)o).getInputStream();
			}
		}
		if( update == null ){
			throw new UploadException("No update data");
		}
		// This parameter hardwires the table based on the construction tag.
		String target_tag = conn.getInitParameter(mode+".target");
		if( target_tag == null){
			// If not set look for table parameter
			target_tag = (String)parameters.get("table");
		}
		if( target_tag == null) {
			throw new UploadException("No target specified for "+mode);
		}
		// Start by looking for an explict UploadParseTarget
		UploadParseTarget target = conn.makeObject(UploadParseTarget.class, target_tag);
		if( target == null ){
				throw new UploadException("Invalid Target");
		}
		UploadParseTargetUpdater updater = new UploadParseTargetUpdater(conn, target);
		return updater.receiveData(parameters, update);
	}
}