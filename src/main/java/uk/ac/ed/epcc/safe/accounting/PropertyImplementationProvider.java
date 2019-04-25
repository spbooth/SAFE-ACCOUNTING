package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;

public interface PropertyImplementationProvider {
	 /** Describe implementation of a property.
     * Used by accounting_properties.jsp
     * @param tag
     * @return
     */
	public String getImplemenationInfo(PropertyTag<?> tag);
}
