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
package uk.ac.ed.epcc.safe.accounting.formatters.value;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
/**
 * <p>
 * An interface that denotes the implementing object is capable of converting
 * objects of a certain type into strings. Along with the constraints applied by
 * this interface, all classes implementing this interface must define a
 * no-argument constructor which properly constructs the parser. If no actual
 * configuration is required, a constructor may be omitted entirely and the
 * objects default constructor provided automatically by Java will be used
 * instead.
 * </p>
 * If the class also provides the reverse operation (parsing a string into an object
 * it should implement {@link ValueParser}.
 * @author jgreen4
 * 
 * @param <F>
 *          The type of object this formatter converts into a
 *          <code>String</code>
 */
public interface ValueFormatter<F> {

	/**
	 * Returns the <code>Class</code> all objects acceptable as input to the format method must be
	 * assignable to.
	 * 
	 * @return The Type of this <code>ValueFormatter</code>
	 */
	public Class<F> getType();

	/**
	 * Formats the specified object into an appropriate <code>String</code>
	 * 
	 * @param object
	 *          The object to format
	 * @return The string representation of <code>object</code>
	 * @throws NullPointerException
	 *           If <code>object<code> is <code>null</code> and this parser does
	 *           not format <code>null</code> objects.
	 */
	public String format(F object);
}