// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import uk.ac.ed.epcc.safe.accounting.parsers.value.DomValueParser;
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/** Annotation to define a {@link DomValueParser} class
 * to parse the element.
 * @author spb
 *
 */
public @interface ParseClass {
   Class<? extends DomValueParser<?>> parser();
}