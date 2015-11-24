// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.update;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;


/** A policy object.
 * 
 * These take part in the parse process but rather than handle the input directly they
 * apply transformations on the properties generated earlier in the process.
 * 
 * @author spb
 *
 */
public interface PropertyContainerPolicy extends PropertyContainerUpdater {

	/**
	 * Generate additional properties based on properties from the basic parse
	 * and previously applied Policies
	 * 
	 * @param rec
	 *            PropertyMap being modified
	 * @throws AccountingParseException
	 */
	public void parse(PropertyMap rec) throws AccountingParseException;
	
	
}