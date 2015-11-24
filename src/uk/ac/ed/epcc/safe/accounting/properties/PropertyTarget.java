// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.properties;


/** An object where the internal state can be accessed as properties.
 * @see PropertyTargetFactory
 * 
 * @author spb
 *
 */
public interface PropertyTarget {

	/** Get property with default.
	 * If the property is not supported by the container of if no value has been set
	 * then the default value is returned.
	 * 
	 * @param <T> Type of property
	 * @param tag PropertTag identifying property
	 * @param def default value
	 * @return property value or default.
	 */
	public abstract <T> T getProperty(PropertyTag<T> tag, T def);

}