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
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.webapp.model.data.Composable;

/** Common interface for classes that receive upload data.
 * This is the class implemented by the actual destination factory or composite
 * that will contain a nested parser that implements {@link PropertyContainerParser}.
 * 
 * @author spb
 * @see PropertyContainerParser
 * @param <R> Type of intermediate record for parse
 */
public interface PropertyContainerParseTarget<R> extends Composable{
	/** Parse a text line into a DerivedPropertyMap. This will add
	 * derived definitions as well as terminal data values.
	 * 
	 * @param map DerivedPropertyMap to be populated
	 * @param current_line
	 * @return true if ok false if this line should be skipped without error
	 * @throws AccountingParseException
	 */
	public abstract boolean parse(DerivedPropertyMap map, R current_line)
			throws AccountingParseException;

	/** Get the underlying {@link PropertyContainerParser} 
	 * 
	 * This is needed to split the input records.
	 * 
	 * @return {@link PropertyContainerParser}
	 */
	public abstract PropertyContainerParser<R> getParser();
	
	/** get the final set of derived properties defined
	 * 
	 * @return
	 */
	public abstract PropExpressionMap getDerivedProperties();
	/** Start a batch parse. This allocates any temporary state.
     * Some properties may be set globally for all records in the parse
	 * rather than provided record by record. The startParse method is passed all known properties of this type 
	 * as they may be useful in setting up the parse. There is no requirement to process these in any way.
	 * Any that are used should be persisted to the database in case we want to rescan the input and this property 
	 * needs to be recoverable to allow the parse to be re-initialised. 
	 * 
	 * 
	 * @param defaults PropertyMap of default global properties 
	 * @throws Exception 
	 */
public void startParse(PropertyMap defaults) throws Exception ;
/** Complete a batch parse. 
 * 
 * @return StringBuilder containing any status report
 */
public StringBuilder endParse();

/** Get the PropertyFinder corresponding to this parse
 * 
 * @return PropertyFinder
 */
public abstract PropertyFinder getFinder();
}