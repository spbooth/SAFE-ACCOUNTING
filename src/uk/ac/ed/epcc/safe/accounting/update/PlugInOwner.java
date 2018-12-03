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
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
import uk.ac.ed.epcc.webapp.model.data.Composable;
/** A class that supports Data upload using plug-ins
 * @see PropertyContainerUpdater
 * 
 * @author spb
 *
 */
public interface PlugInOwner<R> extends PropertyTargetFactory,Composable {

	/** get the {@link PropertyContainerParser} used for the initial parse stage
	 * 
	 * @return PropertyContainerParser
	 */
	public abstract PropertyContainerParser<R> getParser();

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