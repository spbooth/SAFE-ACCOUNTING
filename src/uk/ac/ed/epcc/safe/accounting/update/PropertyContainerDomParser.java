// Copyright - The University of Edinburgh 2011
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