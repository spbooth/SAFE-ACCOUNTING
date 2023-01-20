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
package uk.ac.ed.epcc.safe.accounting.history;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.db.ExpressionTargetFactoryComposite;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.HistoryFactory;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.time.TimePeriod;

/** This class extends {@link HistoryFactory} for peer classes that are {@link ExpressionTarget}s.
 * This class also acts as a UsageProducer allowing the history data to be used in reports. 
 * 
 * @author spb
 *
 * @param <T>
 * @param <F>
 * @param <H> 
 */


public class PropertyTargetHistoryFactory<T extends DataObject , F extends DataObjectFactory<T>, H extends PropertyTargetHistoryFactory.HistoryUse<T>> 
extends HistoryFactory<T,H> {
	private ExpressionTargetFactoryComposite<H> etf = new ExpressionTargetFactoryComposite<>(this);
	private PropertyHistoryComposite<T, F, H> history_prop = new PropertyHistoryComposite<>(this);
	/** HistoryRecord extended to be an ExpressionTarget
	 * 
	 * @author spb
	 *
	 * @param <T>
	 */
	public static  class HistoryUse<T extends DataObject> extends HistoryFactory.HistoryRecord<T> implements TimePeriod{
		
		public HistoryUse(PropertyTargetHistoryFactory<T,?,?> fac,Record res) {
			super(fac, res);
		}

		
		public Date getEnd() {
			return getEndTimeAsDate();
		}		

		public Date getStart()  {
			return getStartTimeAsDate();
		}
		
	

	}
	
	
	
	public PropertyTargetHistoryFactory(F fac, String table) {
		super(fac);
		setContext(fac.getContext(), table);
	}
	/** Constructor for a directly constructed history factory.
	 * As with {@link HistoryFactory} the peer reference property
	 * <i>MUST</i> be set
	 * 
	 * @param conn
	 * @param table
	 */
	public PropertyTargetHistoryFactory(AppContext conn, String table) {
		super(conn,table);
	}
	

	@Override
	protected H makeBDO(Record res) throws DataFault {
		
		return (H) new HistoryUse<T>(this, res);
	}

	
	
	
}