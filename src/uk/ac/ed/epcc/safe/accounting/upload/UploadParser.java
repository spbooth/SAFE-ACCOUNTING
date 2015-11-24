// Copyright - The University of Edinburgh 2011
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