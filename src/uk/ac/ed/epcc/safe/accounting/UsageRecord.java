// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;



import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.webapp.AppContext;


/** Interface implemented by all individual usage records.
 * 
 * Typically usage records will have properties of type {@link Date}
 * that define when the usage occured.
 * @author spb
 *
 */
public interface UsageRecord extends PropertyContainer, ExpressionTarget{
	
    /** Unique key for populating a Table 
	 * Note the Table may have entries from more than one {@link UsageProducer}
	 *  @return Object unique to this table and record
	 */
	public Object getKey();
	/** get AppContext
	 * 
	 * @return AppContext
	 */
    public AppContext getContext();
    /** get identifying string.
     * e.g. for pull-down menus.
     * 
     * @return
     */
    public String getIdentifier();
}