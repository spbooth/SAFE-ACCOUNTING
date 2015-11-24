// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.update;

import uk.ac.ed.epcc.safe.accounting.UsageRecord;

/** A PropertyContainerParser for data where the UsageRecordData
 * is composed of multiple event records.
 * Each event record needs to specify the unique properties to allow
 * the partial record to be identified correctly. 
 * 
 * @author spb
 *
 */
public interface IncrementalPropertyContainerParser extends
		PropertyContainerParser {
	/** Are all the necessary properties present in the record.
	 * Once this method resolves as true the parse process will 
	 * assume the record is complete and not update again.
	 * Note that the properties checked for in this method MUST 
	 * be persisted in the database.
	 * 
	 * @param record
	 * @return boolean true if record is complete
	 */
	public boolean isComplete(UsageRecord record);
	
	/** Once record has been marked as complete apply any side effects.
	 * This occurs immediately before the policy postComplete
	 *  
	 * @param record
	 * @throws Exception 
	 */
	public void postComplete(UsageRecord record) throws Exception;
}