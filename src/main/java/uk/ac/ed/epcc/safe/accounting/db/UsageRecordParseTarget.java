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
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.upload.UploadException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

/** Interface extending  {@link PropertyContainerParseTarget} for parsing Usage records.
 * Usage records are normally parsed once with duplicate values being ignored.
 * @author spb
 * @see AccountingUpdater
 * @param <R> type of intermediate record.
 *
 */
public interface UsageRecordParseTarget<R> extends PropertyContainerParseTarget<R>{

	

	

//	/** Generate a unique ID string for a record that is compatible with
//	 * the {@link #findDuplicate(uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory.Use)} method.
//	 * 
//	 * @param r
//	 * @return String
//	 * @throws Exception
//	 */
//	public abstract String getUniqueID(T r) throws Exception;
	/** Is this a complete or partial record.
	 * 
	 * @param r
	 * @return boolean
	 */
	public abstract boolean isComplete(ExpressionTargetContainer r);

	/** Delete an existing Record including any required side-effects
	 * @param old_record
	 * @throws Exception
	 * @throws DataFault
	 */
	public abstract void deleteRecord(ExpressionTargetContainer old_record) throws Exception, DataFault;

	/** Commit a new record also apply any required side-effects
	 * As the record may only contain a sub-set of the known properties
	 * the full PropertyContainer is passed separately
	 * @param map  Full PropertyContainer for new record
	 * @param record  new record to commit
	 * @return true if record was complete
	 * @throws DataFault
	 */
	public abstract boolean commitRecord(PropertyMap map,ExpressionTargetContainer record)
			throws DataFault;

	/** Perform a full update of an existing record. This includes applying
	 * the side-effects corresponding to record delete and create 
	 * 
	 * @param map
	 * @param record
	 * @return true if record changed
	 * @throws DataFault
	 * @throws Exception 
	 */
	public abstract boolean updateRecord(DerivedPropertyMap map, ExpressionTargetContainer record)
			throws DataFault, Exception;
	/** Create an uncommitted DB record from the property map.
	 * 
	 * @param map
	 * @return Uncommitted record
	 * @throws DataFault
	 * @throws InvalidPropertyException
	 * @throws AccountingParseException
	 */
	public abstract ExpressionTargetContainer prepareRecord(DerivedPropertyMap map) throws DataFault,
			InvalidPropertyException, AccountingParseException;
	
	/** Should replacement of the existing record with new values be allowed.
	 * 
	 * This is an extension point to allow policies to detect refunded jobs and
	 * veto a replacement.
	 * 
	 * @param map  {@link DerivedPropertyMap} of new parsed values
	 * @param record existing record
	 * @return boolean
	 */
	public abstract boolean allowReplace(DerivedPropertyMap map, ExpressionTargetContainer record);

	
	public static <R> UsageRecordParseTarget<R> getParseTarget(AppContext conn,String table){
		UsageRecordParseTarget parse_target = conn.makeObject(UsageRecordParseTarget.class, table);
        if( parse_target == null ){
        	DataObjectFactory<?> fac = conn.makeObject(DataObjectFactory.class,table);
        	if( fac != null) {
        		PropertyContainerParseTargetComposite comp = fac.getComposite(PropertyContainerParseTargetComposite.class);
        		if( comp != null && comp instanceof UsageRecordParseTarget) {
        			parse_target = (UsageRecordParseTarget) comp;
        		}
        	}
        }
        return parse_target;
	}
}