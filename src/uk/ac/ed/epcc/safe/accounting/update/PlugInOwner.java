// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
/** A class that supports Data upload using plug-ins
 * @see PropertyContainerUpdater
 * 
 * @author spb
 *
 */
public interface PlugInOwner extends PropertyTargetFactory {

	/** get the {@link PropertyContainerParser} used for the initial parse stage
	 * 
	 * @return PropertyContainerParser
	 */
	public abstract PropertyContainerParser getParser();

	/** Get a set of {@link PropertyContainerPolicy} to be applied to the
	 * property stream. 
	 * 
	 * @return Set of policies
	 */
	public abstract Set<PropertyContainerPolicy> getPolicies();

	/** Get a set of derived properties (defined as a {@link PropExpressionMap})
	 * that should be implemented by the record.
	 * 
	 * @return PropExpressionMap
	 */
	public abstract PropExpressionMap getDerivedProperties();
	
	/** get the configuration tag corresponding to this owner.
	 * Usually this is the same as the factory configuration tag.
	 * 
	 * @return tag
	 */
	public String getTag();
}