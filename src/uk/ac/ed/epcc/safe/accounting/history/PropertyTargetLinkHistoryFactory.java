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

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FalseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.jdbc.table.IntegerFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.IndexedLinkManager;
import uk.ac.ed.epcc.webapp.model.history.LinkHistoryHandler;
import uk.ac.ed.epcc.webapp.model.history.LinkHistorySQLFilter;



public class PropertyTargetLinkHistoryFactory<L extends Indexed, R extends Indexed, T extends IndexedLinkManager.Link<L,R>,H extends PropertyTargetHistoryFactory.HistoryUse<T>>
extends
		PropertyTargetHistoryFactory<T, IndexedLinkManager<T, L, R>, H> implements LinkHistoryHandler<L, R, T>{

	public PropertyTargetLinkHistoryFactory(IndexedLinkManager<T,L,R> fac, String table) {
		super(fac, table);
	}
	@Override
	public TableSpecification getDefaultTableSpecification(AppContext c,
			String homeTable) {
		TableSpecification spec = super.getDefaultTableSpecification(c, homeTable);
		// though link fields are optional better to have them
		IndexedLinkManager<T, L, R> fac = (IndexedLinkManager<T, L, R>) getPeerFactory();
		if( fac != null){
			spec.setField(fac.getLeftField(), new IntegerFieldType());
			spec.setField(fac.getRightField(), new IntegerFieldType());
		}
		return spec;
	}
	@SuppressWarnings("unchecked")
	public IndexedLinkManager<T,L,R> getLinkManager(){
		return (IndexedLinkManager<T, L, R>) getPeerFactory();
	}
    /* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.history.LinkHistoryHandler#getHistoryFilter(L, R, java.util.Date, java.util.Date)
	 */
    public SQLFilter<H> getHistoryFilter(L left, R right,Date start,Date end) throws DataException{
		T peer=null;
		if( left != null && right != null ){
			peer = getLinkManager().getLink(left, right);
			if( peer == null ){
				// no result
				return new FalseFilter<H>(getTarget());
			}
		}
	    HistoryFilter fil = new HistoryFilter(peer,start,end);
	    fil.addFilter(new LinkHistorySQLFilter<L, R, T,H,PropertyTargetLinkHistoryFactory<L, R, T,H>>(this, left,right));
	   
	   return fil;
	}
    
    /* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.history.LinkHistoryHandler#canLeftJoin()
	 */
    public boolean canLeftJoin(){
    	return res.hasField(getLinkManager().getLeftField());
    }

    /* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.history.LinkHistoryHandler#canRightJoin()
	 */
    public boolean canRightJoin(){
    	return res.hasField(getLinkManager().getRightField());
    }
}