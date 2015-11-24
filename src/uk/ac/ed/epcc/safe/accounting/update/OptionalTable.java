// Copyright - The University of Edinburgh 2011
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