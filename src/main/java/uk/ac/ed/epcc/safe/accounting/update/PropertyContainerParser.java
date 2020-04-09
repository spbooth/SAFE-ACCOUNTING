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

import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.PropertyContainerParseTarget;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;


/** PropertyContainerUpdater that parses a text usage record into a PropertyMap
 * 
 * This is the interface implemented by the parser itself rather than the target class.
 * 
 * @see IncrementalPropertyContainerParser
 * @see PropertyContainerParseTarget
 * @author spb
 * @param <R> type of intermediate record
 *
 */
public interface PropertyContainerParser<R> extends PropertyContainerUpdater{
	
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
  public boolean parse(DerivedPropertyMap map,R record) throws AccountingParseException;
  /** Split multiple usage records into individual usage records.
   * 
   * Though the individual records are usually also represented as Strings they can
   * also use some intermediate representation such as a DOM tree or JSON object.
   * 
   * @param update {@link InputStream} containing multiple records
   * @return Iterator<R> over individual records
   * @throws AccountingParseException 
   */
  public Iterator<R> splitRecords(InputStream update) throws AccountingParseException;
 
  /** format the record back to a String.
   * 
   * The output format should be compatible with the input to {@link #getRecord(String)}.
   * 
   * @param record
   * @return String representation of a single record.
   */
  public String formatRecord(R record);
  /** convert a single record into internal representation
   * 
   * @param text
   * @return R
   */
  public R getRecord(String text);
  /** Get the default set of unique properties for this parser
   * 
   * @return Set of PropertyTag or null
   */
  public Set<PropertyTag> getDefaultUniqueProperties();
}