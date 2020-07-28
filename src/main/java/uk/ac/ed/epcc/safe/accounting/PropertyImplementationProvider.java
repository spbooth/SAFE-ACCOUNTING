package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;

public interface PropertyImplementationProvider {
	 /** Describe implementation of a property.
     * Used by accounting_properties.jsp
     * @param tag
     * @return description of property implementation
     */
	public String getImplemenationInfo(PropertyTag<?> tag);
}
