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
import java.lang.annotation.*;
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/** mark a static field as being a {@link PropertyTag} defining a value that should be present in
 * the optional table specification.
 * 
 * To avoid complicates side effects it is best to not use these in superclasses.
 * However as the tag applies to a field you can just include a copy.
 */
public @interface OptionalTable {
	/** Specify the desired target class (This may be more 
	 * restrictive than the value allowed by the PropertyTag).
	 * 
	 * @return Class
	 */
  Class target() default Object.class;
  /** Specify the target length of a string property
   * 
   * @return length
   */
  int length() default 32;
  
}