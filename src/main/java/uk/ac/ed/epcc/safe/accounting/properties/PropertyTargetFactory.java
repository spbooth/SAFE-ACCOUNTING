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
package uk.ac.ed.epcc.safe.accounting.properties;


/** Factory for objects that support PropertyTags.
 * @see PropertyTarget
 * @author spb
 *
 */
public interface PropertyTargetFactory {
	/** Produce a PropertyFinder that can find any of the PropertyTags
	 * that can be specified for the objects generated by this type.
	 * The PropertyFinder may also contain additional PropertyTags as well.
	 * 
	 * @return PropertyFinder
	 */
	public PropertyFinder getFinder();
	
	
}