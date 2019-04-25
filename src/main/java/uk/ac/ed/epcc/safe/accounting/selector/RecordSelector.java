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
package uk.ac.ed.epcc.safe.accounting.selector;

import uk.ac.ed.epcc.webapp.AppContext;


/** A class that encodes a selection expression for a set of objects based on properties.
 *
 * Implementing classes should override {@link #equals(Object)} and {@link #hashCode()}
 * They should also not reference {@link AppContext} so they can be cached in the user session.
 * @author spb
 *
 */
public interface RecordSelector{
	public <R> R visit(SelectorVisitor<R> visitor) throws Exception;
	
	/** return a copy of the RecordSelector.
	 * This may be a self reference if the object is immutable. 
	 * Normally implementing sub-classes should be immutable (calling copy on
	 * any RecordSelector sub-arguments. In this case the return type should be narrowed to
	 * the sub-type.
	 * 
	 * @return RecordSelector
	 */
	public RecordSelector copy();
	
	public boolean equals(Object peer);
}