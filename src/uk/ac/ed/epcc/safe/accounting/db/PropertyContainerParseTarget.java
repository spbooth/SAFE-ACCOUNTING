// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.model.data.DataObject;

/** Common interface for classes that generate upload data.
 * 
 * @author spb
 *
 * @param <T>
 */
public interface PropertyContainerParseTarget<T extends DataObject & PropertyContainer> {
	/** Parse a text line into a DerivedPropertyMap. This will add
	 * derived definitions as well as terminal data values.
	 * 
	 * @param map DerivedPropertyMap to be populated
	 * @param current_line
	 * @return true if ok false if this line should be skipped without error
	 * @throws AccountingParseException
	 */
	public abstract boolean parse(DerivedPropertyMap map, String current_line)
			throws AccountingParseException;

	/** split update text into a series of lines.
	 * @param update
	 * @return Iterator<String>
	 * @throws AccountingParseException
	 */
	public abstract Iterator<String> splitRecords(String update)
			throws AccountingParseException;
	
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