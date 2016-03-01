//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.allocations;

import java.util.Date;
import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OrderClause;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.NumberOp;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.exceptions.ValidateException;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.period.SequenceManager;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
/** A {@link AllocationFactory} where the allocations form non-overlapping sequences
 * 
 * @author spb
 *
 * @param <T>
 */
public class SequenceAllocationFactory<T extends AllocationFactory.AllocationRecord,R> extends AllocationFactory<T,R>  implements SequenceManager<T>{

	public SequenceAllocationFactory(AppContext c, String table) {
		super(c, table);
	}
	@SuppressWarnings("unchecked")
	public boolean sameSequence(T a, T b) throws InvalidExpressionException{
		for(ReferenceTag tag : getIndexProperties()){
			IndexedReference a_ref = (IndexedReference) a.getProperty(tag);
			IndexedReference b_ref = (IndexedReference) b.getProperty(tag);
			if( ! a_ref.equals(b_ref)){
				return false;
			}
		}
		return true;
	}
	public boolean canMerge(T first, T second) {
		if(! super.canMerge(first, second)){
			return false;
		}
		if( first.getEnd().equals(second.getStart()) || first.getStart().equals(second.getEnd())){
			try {
				return sameSequence(first, second);
			} catch (InvalidExpressionException e) {
				getLogger().error("Error checking sequence",e);
				return false;
			}
		}
		return false;
	}
	public T merge(T first, T second) throws Exception {
		Date f_start = first.getStart();
		Date f_end = first.getEnd();
		Date s_start = second.getStart();
		Date s_end = second.getEnd();
		
		Date start = f_start;
		if( start.after(s_start)){
			start=s_start;
		}
		Date end = s_end;
		if( end.before(f_end)){
			end=f_end;
		}
		for(PropertyTag tag : getSplitProperties()){
			first.setProperty(tag, NumberOp.add((Number)first.getProperty(tag), (Number)second.getProperty(tag)));			
		}
		first.setProperty(StandardProperties.STARTED_PROP, start);
		first.setProperty(StandardProperties.ENDED_PROP, end);
		notifyMerge(first, second);
		second.delete();
		first.commit();
		return first;
	}
	
	public T getNextInSequence(T current, boolean move_up) {
		try{
			AndRecordSelector sel = new AndRecordSelector();
			if( move_up ){
				sel.add(new SelectClause<Date>(StandardProperties.STARTED_PROP,MatchCondition.GT,current.getStart()));
				sel.add(new OrderClause<Date>(false, StandardProperties.STARTED_PROP));
			}else{
				sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.LT,current.getEnd()));
				sel.add(new OrderClause<Date>(true, StandardProperties.ENDED_PROP));
			}
			for(ReferenceTag t : getIndexProperties()){
				sel.add(new SelectClause<IndexedReference>(t,current));
			}
			Iterator<T> it = getIterator(sel, 0, 1);
			if( it.hasNext()){
				return it.next();
			}
			return null;
		}catch(Exception e){
			getLogger().error("Error in move-up",e);
			return null;
		}
	}
	public void setStart(T period, Date d) throws Exception {
		period.setProperty(StandardProperties.STARTED_PROP, d);
		period.commit();
	}
	public void setEnd(T period, Date d) throws Exception {
		period.setProperty(StandardProperties.ENDED_PROP, d);
		period.commit();
	}

	public boolean isEmpty(TimePeriod period, PropertyContainer sequence) throws Exception{
		AndRecordSelector sel = new AndRecordSelector();
		for(ReferenceTag tag : getIndexProperties()){
			sel.add(new SelectClause(tag, sequence));
		}
		sel.add(new PeriodOverlapRecordSelector(period,StandardProperties.STARTED_PROP,StandardProperties.ENDED_PROP));
		return getRecordCount(sel) ==0;
	}
	public class SequenceCreationValidator extends AllocationValidator{

		@Override
		public void validate(Form f) throws ValidateException {
			super.validate(f);
			Date start=(Date)f.get(StandardProperties.STARTED_TIMESTAMP);
			Date end=(Date)f.get(StandardProperties.COMPLETED_TIMESTAMP);
			TimePeriod period = new Period(start,end);
			PropertyMap seq = new PropertyMap();
			AccessorMap map = getAccessorMap();
			for(ReferenceTag tag : getIndexProperties()){
				String field = map.getField(tag);
				try {
					tag.set(seq,(Indexed) f.getItem(field));
				} catch (InvalidPropertyException e) {
					throw new ValidateException("Internal error - bad property", e);
				}
			}
			boolean empty=false;
			try {
				empty = isEmpty(period, seq);
			} catch (Exception e) {
				throw new ValidateException("Internal error", e);
			}
			if( ! empty){
				throw new ValidateException("Record overlapps with existing allocation");
			}
		}
		
	}
	
	@Override
	protected void addCreationValidator(Form f) {
		super.addCreationValidator(f);
		f.addValidator(new SequenceCreationValidator());
	}
	public void canChangeStart(T current, Date d) throws ValidateException {
		PropertyMap map = new PropertyMap();
		map.setAll(current);
		map.setProperty(StandardProperties.STARTED_PROP, d);
		canModify(current, map);
		
	}
	public void canChangeEnd(T current, Date d) throws ValidateException {
		PropertyMap map = new PropertyMap();
		map.setAll(current);
		map.setProperty(StandardProperties.ENDED_PROP, d);
		canModify(current, map);		
	}
}