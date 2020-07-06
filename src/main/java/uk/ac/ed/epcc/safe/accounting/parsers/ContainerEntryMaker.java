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
package uk.ac.ed.epcc.safe.accounting.parsers;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;

/** A nested parser that parses a single field or fragment of record generating proeprty values.
 * <p>
 * Classes implementing this interface have the ability to add values to a
 * <code>PropertyContainer</code>. <code>PropertyContainer</code>s contain
 * objects of various types. <code>ContainerEntryMaker</code>s take strings as
 * arguments for the values to set in the <code>PropertyContainer</code>. The
 * implication is that the maker will parse the string and convert it into the
 * appropriate type. A <code>ContainerEntryMaker</code> will also select,
 * generate be given at an earlier stage, a <code>PropertyTag</code> with the
 * appropriate generics information to store the generated value in the
 * container.
 * </p>
 * <code>ContainerEntryMaker</code>s may add zero one or more entries to a
 * container. How many are added depends on the implementation. If a simple
 * one-to-one relationship is required between <code>PropertyTag</code> and
 * parsed value, the implementing class {@link PropertyEntryMaker} should be
 * used. If more complicated mappings are required, it may be better to write a
 * specific (ie non generalised) implementation for the mapping in question.
 * <p>
 * It is also possible for a {@link ContainerEntryMaker} to throw an {@link AccountingParseException} to
 * abort an entire record parse (for example success/fail field might generate a {@link SkipRecord}.
 * @author jgreen4
 * 
 */
public interface ContainerEntryMaker
{
  /**
   * Parses the specified string and adds the generated value to
   * <code>container</code>. The value may be added using one or more
   * <code>PropertyTag</code>s depending on the data stored in
   * <code>valueString</code>.
   * 
   * @param contanier
   *          The container in which to place data extracted from
   *          <code>valueString</code>
   * @param valueString
   *          The string to parse extract data from
   * @throws IllegalArgumentException
   *           If <code>valueString</code> is not of a format that allows this
   *           maker to extract data from it
   * @throws InvalidPropertyException
   *           If the container cannot contain <code>PropertyTag</code>s used by
   *           this entry maker to store parsed data <code>PropertyTag</code>
   * @throws NullPointerException
   *           If <code>container</code> is <code>null</code> or valueString is
   *           null and this the parser does not support <code>null</code>
   *           strings
   */
  public void setValue(PropertyContainer contanier, String valueString)
    throws IllegalArgumentException,
    InvalidPropertyException,
    NullPointerException,
    AccountingParseException;

  /**
   * Parses the specified string and adds the generated value to
   * <code>container</code>. The value may be added using one or more
   * <code>PropertyTag</code>s depending on the data stored in
   * <code>valueString</code>. This method differs from
   * {@link #setValue(PropertyContainer, String)} in that it can't throw an
   * <code>InvalidPropertyException</code> (<code>PropertyMap</code>s don't when
   * properties are set in them, unlike <code>PropertyContainer</code>s)
   * 
   * @param map
   *          The container in which to place data extracted from
   *          <code>valueString</code>
   * @param valueString
   *          The string to parse extract data from
   * @throws IllegalArgumentException
   *           If <code>valueString</code> is not of a format that allows this
   *           maker to extract data from it
   * @throws NullPointerException
   *           If <code>container</code> is <code>null</code> or valueString is
   *           null and this the parser does not support <code>null</code>
   *           strings
   */
  public default void setValue(PropertyMap map, String valueString)
    throws IllegalArgumentException,
    NullPointerException,
    AccountingParseException{
	  try {
		setValue((PropertyContainer) map, valueString);
	} catch (InvalidPropertyException e) {
		throw new ConsistencyError("Unexpected exception", e);
	} 
  };
}