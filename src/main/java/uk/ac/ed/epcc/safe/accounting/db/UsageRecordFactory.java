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

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.CurrentTimeService;
import uk.ac.ed.epcc.webapp.jdbc.table.DateFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.time.TimePeriod;


/** Base class for different accounting factories.
 * This class defines a base class for all DB persisted {@link ExpressionTargetContainer}s for normal accounting.
 * The assumption being that accounting records need no additional functionality as they are only accessed
 * via the usage producer.
* Eventually it should be possible to make this a final inner class but currently it is a 
* static class to allow controlled migration from the original design where each 
* accounting scheme had its own class. 
 * 
 * Classes that need to present to the reporting system but <em>DO</em> have additional behaviour should
 * use a superclass or implement the interfaces directly.
 * @author spb
 * @param <T> class of UsageRecord
 *
 */

public abstract class UsageRecordFactory<T extends UsageRecordFactory.Use> extends
		DefaultUsageProducer<T> {
	

	public static class Use extends DataObjectPropertyContainer implements TimePeriod {
		
		protected Use(UsageRecordFactory fac,Record r) {
			super(fac,r);
		}

		protected Repository.Record getRecord() {
			// used for unit testing
			return record;
		}
		public Date getEnd() {
			return getProxy().getProperty(StandardProperties.ENDED_PROP,null);
		}		

		
		public Date getStart()  {
			return getProxy().getProperty(StandardProperties.STARTED_PROP,null);
		}
		
		/** get the fraction of the {@link TimePeriod} that is elapsed.
		 * 
		 * @return fraction
		 */
		public double getElapsedFraction(){
			CurrentTimeService time = getContext().getService(CurrentTimeService.class);
			Date point = time.getCurrentTime();
			Date start = getStart();
			Date end = getEnd();
			
			if( start == null || end == null){
				return 0.0;
			}
			if( point.before(start)){
				return 0.0;
			}
			if( point.after(end)){
				return 1.0;
			}
			return ((double)(point.getTime()-start.getTime()))/((double)(end.getTime()-start.getTime()));
		}
		

		/** Unique key for populating a Table 
		 * Note the Table may have entries from more than one table
		 *  @return Object unique to this table and record
		 */
		public final Object getKey() {
			// implicit dependency with AccountingManager.AccountingRecordUpdator
			return getFactoryTag() +":"+ getID();
		}

		
		

		public void setInsertedTime(Date start) {
			record.setOptionalProperty(INSERTED_TIMESTAMP, start);
		}
		public UsageRecordFactory getUsageRecordFactory(){
			return (UsageRecordFactory) getFactory();
		}

//		
		
		
	}

	//protected static final String COMPLETED_TIMESTAMP_FIELD = "CompletedTimestamp";

	//protected static final String STARTED_TIMESTAMP_FIELD = "StartedTimestamp";

	protected static final String INSERTED_TIMESTAMP = "InsertedTimestamp"; //optional
    
	protected UsageRecordFactory(){
		
	}

	
	@Override
	protected DataObject makeBDO(Record res) throws DataFault {
		return new Use(this,res);
	}
	@Override
	public Class<T> getTarget(){
		return (Class) Use.class;
	}


	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c, String table) {
		TableSpecification spec = new TableSpecification();
		spec.setOptionalField(INSERTED_TIMESTAMP, new DateFieldType(true, null));
		return spec;
	}
	
	
	

}