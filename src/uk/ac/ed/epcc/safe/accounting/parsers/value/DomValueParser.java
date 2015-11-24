// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import org.w3c.dom.Node;

/** Interface that parses a Dom Node to generate a value. 
 * Though in many cases this may map onto an underlying ValueParser this interface allows
 * more complex parses such as adjusting units based on an attribute value.
 * 
 * @author spb
 *
 * @param <T>
 */
public interface DomValueParser<T> {
	/**
	 * Returns the <code>Class</code> of object this parser returns with it's
	 * {@link #parse(Node)} method is invoked. The returned class will be the
	 * exact <code>Class</code> (i.e. not a subclass) of this
	 * <code>ValueParser</code>'s type
	 * 
	 * @return The Type of this <code>DomValueParser</code>
	 */
	public abstract Class<T> getType();

	/**
	 * Parses the specified Node and constructs the appropriate object out of
	 * it.
	 * @param valueNode Node to parse
	 * @return The object representation of <code>valueNode</code>
	 * @throws ValueParseException
	 */
	public abstract T parse(Node valueNode) throws ValueParseException; 
}