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

import org.w3c.dom.Node;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;

/** An XML oriented parser. A single class may implement both this interface and 
 * {@link PropertyContainerParser} by having the string parse method parse the
 * string into a DOM tree and forward onto the DOM parse method.
 * 
 * @author spb
 *
 */
public interface PropertyContainerDomParser extends PropertyContainerUpdater{
	/** Parse a Dom representation of a UsageRecord in an XML format.
	 * If the parse needs to be aborted throw an AccountingParseException.
	 * 
	 * If the parse cannot succeed but no error needs to be reported
	 * (e.g. an empty or comment line) this method can return false
	 * @param map PropertyMap to add quantities
	 * @param record
	 * @return true if parse ok
	 * @throws AccountingParseException
	 */
  public boolean parse(PropertyMap map,Node record) throws AccountingParseException;
  /** Return the set of PropertyTags that may be defined by the record Node
   * No attempt is made to parse the actual content so this method may be used on a
   * template document where no actual values are specified.
   * @param record
   * @return Set of PropertyTag
   */
  public Set<PropertyTag> defines(Node record);
}