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
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter;


/** <p> 
 * An interface that denotes the implementing object is capable of converting
 * between strings and objects of a certain type. Along with the constraints applied by
 * this interface, all classes implementing this interface must define a
 * no-argument constructor which properly constructs the parser. If no actual
 * configuration is required, a constructor may be omitted entirely and the
 * objects default constructor provided automatically by Java will be used
 * instead.
 * </p>
 * 
 * @author jgreen4
 * 
 * @param <T>
 *          The type of object this parser generates from string values
 */
public interface ValueParser<T> extends ValueFormatter<T>{
	/**
	 * Returns the <code>Class</code> of object this ValueParser can handle.
	 * The {@link #parse(String)} method must return objects that are assignable to this type.
	 * The {@link #format(T)} method can format any object assignable to this type.
	 *  The returned class will be the
	 * exact <code>Class</code> (i.e. not a subclass) of this
	 * <code>ValueParser</code>'s type
	 * 
	 * @return The Type of this <code>ValueParser</code>
	 */
	public abstract Class<T> getType();

	/**
	 * Parses the specified string and constructs the appropriate object out of
	 * it.
	 * 
	 * @param valueString
	 *          The string to parse
	 * @return The object representation of <code>valueString</code>
	 * @throws ValueParseException
	 *           If <code>valueString</code> is not of a format that allows this
	 *           parser to convert it into an object of type T
	
	 */
	public abstract T parse(String valueString) throws ValueParseException;
	
	/** Format a value as a string.
	 * The resulting string must be in a form that can be parsed by this ValueParser
	 * 
	 * @param value
	 * @return String
	 */
	public abstract String format(T value);
}