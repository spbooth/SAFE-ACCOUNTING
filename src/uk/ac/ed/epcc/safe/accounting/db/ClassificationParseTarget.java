// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.model.data.DataObject;


/** Interface for Parse targets that represent mutable data.
 * Existing entries are updated each time they are parsed.
 * 
 * @author spb
 *
 * @param <T>
 */
public interface ClassificationParseTarget<T extends DataObject & PropertyContainer> extends
		PropertyContainerParseTarget<T> {
	/** Returns an uncommitted object updated to match the input container.
	 * This is either a new object or an existing object selected using the
	 * key properties.
	 * 
	 * @param value
	 * @return new or existing object 
	 * @throws AccountingParseException 
	 * 
	 */
	public T make(PropertyContainer value) throws AccountingParseException;
	
	/** extract global properties from post parameters.
	 * 
	 * @param params
	 * @return PropertyMap
	 */
	public PropertyMap getGlobals(Map<String,Object> params);

}