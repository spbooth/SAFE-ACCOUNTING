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
import java.util.LinkedHashMap;

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
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.exceptions.ValidateException;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.table.DateFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableContentProvider;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.period.MergeTransition;
import uk.ac.ed.epcc.webapp.model.period.MoveDateTransition;
import uk.ac.ed.epcc.webapp.model.period.SequenceManager;
import uk.ac.ed.epcc.webapp.model.period.SequenceTransition;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
import uk.ac.ed.epcc.webapp.time.TimePeriodComparator;
/** A {@link AllocationFactory} where the allocations can form non-overlapping sequences.
 * 
 * A sequence is defined by the index properties. Optionally certain sequences may be allowed to contain 
 * overlapping records by overriding {@link #allowOverlap(PropertyContainer)}
 * 
 * @author spb
 *
 * @param <T> type of record
 * @param <R> intermediate record type for parse
 */
public class SequenceAllocationFactory<T extends AllocationFactory.AllocationRecord,R> extends AllocationFactory<T,R>  implements SequenceManager<T>, TableContentProvider{
	private ReferenceTag<T,?> self_tag=null;
	public SequenceAllocationFactory(AppContext c, String table) {
		super(c, table);
	}
	private ReferenceTag<T,?> getSelfTag(){
		if( self_tag == null) {
			self_tag = (ReferenceTag<T, ?>) getFinder().find(IndexedReference.class, getTag());
		}
		return self_tag;
	}
	
	/** Extension point to allow certain sequences to contain overlapping records
	 * 
	 * @param index {@link PropertyContainer} of index properties
	 * @return true if overlap allowed
	 */
	public boolean allowOverlap(PropertyContainer index) {
		return false;
	}
	
	@Override
	public final boolean noOverlapps(T current) {
		return ! allowOverlap(current);
	}
	/** Do two records have matching index properties.
	 * 
	 * @param a
	 * @param b
	 * @return
	 * @throws InvalidExpressionException
	 */
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
			// Canonical sequence ordering is the same as that generated by TimePeriodComparator
			// ordered by start, then by end.
			// However for overlapping records with the same start date
			// the SQL could return a record
			AndRecordSelector sel = new AndRecordSelector();
			sel.add(new SelectClause<>(getSelfTag(),MatchCondition.NE,current)); // never same as current
			if( move_up ){ 
				sel.add(new SelectClause<>(StandardProperties.STARTED_PROP,MatchCondition.GE,current.getStart()));
				sel.add(new OrderClause<>(false, StandardProperties.STARTED_PROP));
				sel.add(new OrderClause<>(false, StandardProperties.ENDED_PROP));
			}else{
				sel.add(new SelectClause<>(StandardProperties.STARTED_PROP,MatchCondition.LE,current.getStart()));
				sel.add(new OrderClause<>(true, StandardProperties.STARTED_PROP));
				sel.add(new OrderClause<>(true, StandardProperties.ENDED_PROP));
			}
			TimePeriodComparator comp = new TimePeriodComparator(! move_up);
			for(ReferenceTag t : getIndexProperties()){
				sel.add(new SelectClause<IndexedReference>(t,current));
			}
			try(CloseableIterator<T> it = getIterator(sel)){
				while( it.hasNext()){
					T cand = it.next();
					if( comp.compare(cand, current) > 0 ) {
						return cand;
					}
				}
			}
			return null;
		}catch(Exception e){
			getLogger().error("Error in move-up",e);
			return null;
		}
	}
	
	public T getMergeCandidate(T current, boolean move_up) {
		try{
			AndRecordSelector sel = new AndRecordSelector();
			if( move_up ){
				sel.add(new SelectClause<>(StandardProperties.STARTED_PROP,current.getEnd()));
				sel.add(new OrderClause<>(false, StandardProperties.STARTED_PROP));
			}else{
				sel.add(new SelectClause<>(StandardProperties.ENDED_PROP,current.getStart()));
				sel.add(new OrderClause<>(true, StandardProperties.ENDED_PROP));
			}
			for(ReferenceTag t : getIndexProperties()){
				sel.add(new SelectClause<IndexedReference>(t,current));
			}
			try(CloseableIterator<T> it = getIterator(sel, 0, 1)){
				if( it.hasNext()){
					return it.next();
				}
			}
			return null;
		}catch(Exception e){
			getLogger().error("Error in find merge candidate",e);
			return null;
		}
	}
	public void setStart(T period, Date d) throws Exception {
		period.setProperty(StandardProperties.STARTED_PROP, d);
		if( period.commit() ) {
			notifyModified(period, "Start changed");
		}
	}
	public void setEnd(T period, Date d) throws Exception {
		period.setProperty(StandardProperties.ENDED_PROP, d);
		if( period.commit() ) {
			notifyModified(period, "End changed");
		}
	}

	public boolean isEmpty(TimePeriod period, PropertyContainer sequence) throws Exception{
		AndRecordSelector sel = new AndRecordSelector();
		for(ReferenceTag tag : getIndexProperties()){
			sel.add(new SelectClause(tag, sequence));
		}
		sel.add(new PeriodOverlapRecordSelector(period,StandardProperties.STARTED_PROP,StandardProperties.ENDED_PROP));
		return ! exists(sel);
	}
	public class SequenceCreationValidator extends AllocationValidator{

		@Override
		public void validate(Form f) throws ValidateException {
			super.validate(f);
			Date start=(Date)f.get(getStartField());
			Date end=(Date)f.get(getEndField());
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
			if( allowOverlap(seq)) {
				return;
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
	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c, String table) {

		TableSpecification spec = super.getDefaultTableSpecification(c, table);
		// If a parser is installed it may set these as well but we want
		// the table to work without a configured parser as it can also be
		// directly manipulated by forms
		spec.setField(StandardProperties.STARTED_TIMESTAMP, new DateFieldType(false, new Date(0L)));
		spec.setField(StandardProperties.COMPLETED_TIMESTAMP, new DateFieldType(false, new Date(Long.MAX_VALUE)));
		return spec;
	}
	@Override
	public void addSummaryContent(ContentBuilder cb) {
		cb.addHeading(3,"Allocation property sets");
		Table t = new Table();
		t.put("Value", "Index properties", getIndexProperties());
		t.put("Value", "Allocation properties", getAllocationProperties());
		t.put("Value", "Split properties", getSplitProperties());
		t.setKeyName("Property");
		cb.addColumn(getContext(), t, "Value");
	}
	@Override
	protected final LinkedHashMap<AllocationKey<T>, Transition<T>> makeTransitions() {
		
		LinkedHashMap<AllocationKey<T>, Transition<T>> result = new LinkedHashMap<>();
		result.put(new ViewAllocationKey<T>(AllocationRecord.class, "<<<"), new SequenceTransition<>(this, this, false));
		result.put(new AllocationKey<T>(AllocationRecord.class,"<Merge"), new MergeTransition<>(this, this, false));
		result.put(new AllocationKey<T>(AllocationRecord.class,"ChangeStart","Change the start date"), new MoveDateTransition<>(this,this,true));
		result.putAll(super.makeTransitions());
		addTransitions(result);
		
		result.put(new AllocationKey<T>(AllocationRecord.class,"ChangeEnd","Change the end date"), new MoveDateTransition<>(this,this,false));

		result.put(new AllocationKey<T>(AllocationRecord.class,"Merge>"), new MergeTransition<>(this, this, true));
		result.put(new ViewAllocationKey<T>(AllocationRecord.class, ">>>"), new SequenceTransition<>(this, this, true));

		return result;
	}
	protected void addTransitions(LinkedHashMap<AllocationKey<T>, Transition<T>> res){
		
	}
}