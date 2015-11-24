// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;


/** PropertyContainerUpdater that parses a text usage record into a PropertyMap
 * 
 * @see IncrementalPropertyContainerParser
 * @author spb
 *
 */
public interface PropertyContainerParser extends PropertyContainerUpdater{
	
	/** Parse a String representation of the UsageRecord.
	 * If the parse needs to be aborted throw and AccountingParseException.
	 * 
	 * If the parse cannot succeed but no error needs to be reported
	 * (e.g. an empty or comment line) this method can return false
	 * @param map PropertyMap to add quantities
	 * @param record
	 * @return true if parse ok
	 * @throws AccountingParseException
	 */
  public boolean parse(PropertyMap map,String record) throws AccountingParseException;
  /** Split multiple usage records into individual usage records
   * 
   * @param update string containing multiple records
   * @return Iterator<String> over individual records
 * @throws AccountingParseException 
   */
  public Iterator<String> splitRecords(String update) throws AccountingParseException;
 
  
  /** Get the default set of unique properties for this parser
   * 
   * @return Set of PropertyTag or null
   */
  public Set<PropertyTag> getDefaultUniqueProperties();
}