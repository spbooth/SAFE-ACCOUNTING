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
/** An UploadParser is the interface that needs to be implemented by a class to allow it to
 * be invoked by the UploadServlet of command line upload app to load data.
 * 
 * @author spb
 *
 */
public interface UploadParser {

	/** process data for upload.
	 * Normal convention is for  the uploaded data to be in a parameter called
	 * <em>upload</em>
	 * 
	 * @param parameters (map of form/app parameters)
	 * @return String status report
	 * @throws UploadException
	 */
	public String upload(Map<String,Object> parameters) throws UploadException;
}