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
package uk.ac.ed.epcc.safe.accounting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import uk.ac.ed.epcc.safe.accounting.db.DefaultAccountingService;
import uk.ac.ed.epcc.safe.accounting.db.DefaultUsageProducer;
import uk.ac.ed.epcc.safe.accounting.db.NarrowTag;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.ContextCached;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.iterator.AbstractMultiIterator;
import uk.ac.ed.epcc.webapp.model.data.iterator.NestedIterator;
import uk.ac.ed.epcc.webapp.timer.TimeClosable;
/** A composite {@link UsageProducer} that combines results from several 
 * underlying {@link UsageProducer}s.
 * 
 * It also supports generating an 
 * 
 * @author spb
 *
 * @param <UR>
 */

public abstract class UsageManager<UR> extends AbstractContexed implements
		UsageProducer<UR> , ContextCached{

	
    public class MultiIterator extends AbstractMultiIterator<UR>{
       

		private Iterator<UsageProducer<UR>> it;
        private RecordSelector sel;
        private int skip,count;
		public MultiIterator(RecordSelector sel, int skip, int count) {
			it=factories.values().iterator();
			this.sel=sel;
			this.skip=skip;
			this.count=count;
		}

		@Override
		protected Iterator<UR> nextIterator() {
			try{
				while( it.hasNext()){
					UsageProducer<UR> prod = it.next();
					if( prod.compatible(sel)){
						if( it.hasNext() && skip > 0){
							// may have to skip whole iterators
							long size = prod.getRecordCount(sel); // number of matching records from this iterator
							while( size <= skip && it.hasNext() ){
								prod=it.next();
								size -= skip;
								size = prod.getRecordCount(sel);
							}
						}
						Iterator<UR> res= prod.getIterator(sel, skip, count);
						skip=0; // all skips should be in first iterator
						return res;
					}
				}
				return null;
			}catch(Exception e){
				getLogger().error("Error in MultiIterator",e);
				return null;		
			}
		}
		 @Override
			public UR next() {
				UR res = super.next();
				// all skips should be complete and iterations sill expected
				assert(skip==0);
				assert(count > 0);
				count--; // set count for next iterator
				return res;
			}

		@Override
		public boolean hasNext() {
			if( count <= 0 ){
				return false;
			}
			return super.hasNext();
		}
    }
	

	
 
	/**
	 * Stores the underlying implementation classes all should Implement
	 * AccountingProducer
	 */
	private final LinkedHashMap<String, UsageProducer<UR>> factories = new LinkedHashMap<>();
	private final LinkedHashMap<String,String> descriptions = new LinkedHashMap<>();
	private final PropExpressionMap map; 
	
	private final String tag;
	public String getTag() {
		return tag;
	}
	/**
	 * Constructor
	 * 
	 * @param c
	 * @param tag 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public UsageManager(AppContext c,String tag) {
		this(c,tag,null);
	}
	protected UsageManager(AppContext c,String tag, Set<UsageProducer<UR>> facs) {	
		super(c);
		this.tag=tag;
		
		// cache the factories list in the AppContext as there is a
		// high probablity of reuse within a request and this hardly ever
		// changes
		
		
		populate(tag);
		if( facs != null ) {
			for(UsageProducer<UR> prod : facs) {
				addProducer(prod.getTag(), prod);
			}
		}
			
		
		PropExpressionMap tmp = null;
		for( UsageProducer<UR> prod : factories.values()){
			if( tmp == null ){
				// initial set from first seen
				// make sure we copy as this map is edited.
				// nested SHOULD return a copy but lets be safe as this has bitten us before
				tmp = new PropExpressionMap( prod.getDerivedProperties()); 
			}else{
				// merge definitions we only want the set common to all producers.

				PropExpressionMap merge = prod.getDerivedProperties(); 
				Iterator<PropertyTag> it = tmp.keySet().iterator();
				while(it.hasNext()){
					PropertyTag t = it.next();
					if( ! merge.containsKey(t) || ! tmp.get(t).equals(merge.get(t))){
						it.remove();
					}
				}
			}
		}
		map=tmp;
	}

	
	public <PT> Set<PT> getValues(PropertyTag<PT> propertyTag, RecordSelector selector)
			throws Exception {
		Set<PT>  result = null;
		for (UsageProducer<UR> prod : factories.values()) {
			if( prod.hasProperty(propertyTag)){
				if (result == null ) {
					result = prod.getValues(propertyTag, selector);

				} else {
					result.addAll(prod.getValues(propertyTag, selector));

				}
			}

		}
		return result;
	}

	
	public long getRecordCount(RecordSelector sel)
	throws DataException {
		long result = 0L;
		for (UsageProducer<UR> prod: factories.values()) {
			try(TimeClosable time= new TimeClosable(conn, ()->getTag()+".getRecordCount."+prod.getTag())){
				if( prod.compatible(sel)){
					result += prod.getRecordCount(sel);
				}
			} catch (Exception e) {
				getLogger().error("Error in getCount",e);
			}

		}
		return result;
	}
	public boolean exists(RecordSelector sel)
			throws DataException {
		for (UsageProducer<UR> prod: factories.values()) {
			try {
				if( prod.exists(sel)){
					return true;
				}
			} catch (Exception e) {
				getLogger().error("Error in exists",e);
			}

		}
		return false;
	}
	public  CloseableIterator<UR> getIterator( RecordSelector sel) throws Exception {
		/*
		 * Generate a Nested Iterator over the combined results of all
		 * Factories.
		 */
		NestedIterator<UR> res = new NestedIterator<>();
		for (UsageProducer<UR> prod: factories.values()) {
			if( prod.compatible(sel)){
				//log.debug("Using "+prod.toString());
				res.add(prod.getIterator(sel));
			}else{
				getLogger().debug("getIterator: selector not compatible with "+prod.toString());
			}
		}
		return res;
	}
	public  CloseableIterator<UR> getIterator( RecordSelector sel,int skip, int count) throws DataFault {
		return new MultiIterator(sel,skip,count);
	}
//	@Deprecated
//	public <I, P> Map<I, P> getPropMap(PropertyTag<I> index,
//			PropertyTag<P> property,  RecordSelector selector)
//			throws Exception  {
//		Map<I, P> result = null;
//		for (UsageProducer<UR> prod : factories.values()) {
//			if( prod.compatible(selector)){
//				if (result == null) {
//					result = prod.getPropMap(index, property,  selector);
//				} else {
//					Map<I, P> merge = prod.getPropMap(index, property, selector);
//					for (I key : merge.keySet()) {
//						// just merge the maps 
//						result.put(key, merge.get(key));
//					}
//				}
//			}
//		}
//		return result;
//	}


	public <R,T,D> Map<R, T> getReductionMap(
			PropExpression<R> tag,ReductionTarget<T,D> res, 
			RecordSelector selector) 
					throws Exception {
		Map<R, T> result = null;
		boolean use_composite = DefaultAccountingService.DEFAULT_COMPOSITE_FEATURE.isEnabled(getContext()) || factories.size() > 1;
		for (UsageProducer<UR> prod: factories.values()) {
			boolean old = prod.setCompositeHint(use_composite);
			try(TimeClosable time= new TimeClosable(conn, ()->getTag()+".getReductionMap."+prod.getTag())){
				if( prod.compatible(tag) && prod.compatible(selector)){
					if (result == null) {
						result = prod.getReductionMap(tag, res, selector);
					} else {
						Map<R, T> merge = prod.getReductionMap(tag, res, selector);
						for (R key : merge.keySet()) {
							result.put(key, res.combine(result.get(key),merge
									.get(key)));
						}
					}
				}
			}finally {
				prod.setCompositeHint(old);
			}
		}

		return result;
	}
	
	
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.UsageProducer#getIndexedSumMap(java.util.List, java.util.List, uk.ac.ed.epcc.safe.accounting.RecordSelector)
	 */
	@SuppressWarnings("unchecked")
	public Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap(
			Set<ReductionTarget> targets,
			RecordSelector selector) throws Exception {
		Map<ExpressionTuple,ReductionMapResult> result = null;
		boolean is_composite = DefaultAccountingService.DEFAULT_COMPOSITE_FEATURE.isEnabled(getContext()) || factories.size() > 1;
		//TODO change avg to sum and count
		for(UsageProducer prod : factories.values()){
			boolean old_comp = prod.setCompositeHint(is_composite);
			try(TimeClosable time= new TimeClosable(conn, ()->getTag()+".getIndexReductionMap."+prod.getTag())){
				// We can substitute default values for non-index reductions
				// that are not supported by a producer
				boolean compatible=true;
				for(ReductionTarget t : targets){
					compatible = compatible &&  (t.getReduction() != Reduction.INDEX || prod.compatible(t.getExpression()));
				}
				compatible = compatible && prod.compatible(selector);
				if( compatible ){
					if( result == null ){
						result = prod.getIndexedReductionMap( targets, selector);
					}else{
						Map<ExpressionTuple,ReductionMapResult> tmp = prod.getIndexedReductionMap( targets, selector);
						for(ExpressionTuple key : tmp.keySet()){
							ReductionMapResult old = result.get(key);
							ReductionMapResult merge = tmp.get(key);
							if( old == null ){
								if( merge != null ){
									result.put(key, merge);
								}
							}else{
								for(ReductionTarget target : targets){
									old.put(target, target.combine(old.get(target),merge.get(target)));
								}
							}
						}
					}
				}

			}finally {
				prod.setCompositeHint(old_comp);
			}
		}
		if( result == null){
			return new HashMap<>();
		}
		return result;
	}

	

	


	@SuppressWarnings("unchecked")
	public <C> Vector<C> getProducers(Class<C> target) {
		Vector<C> res = new Vector<>();
		for (UsageProducer p : factories.values()) {
			if (target.isAssignableFrom(p.getClass())) {
				res.add((C) p);
			}
		}
		return res;
	}

	public String getProducerTag(String key) {
		if (key == null) {
			if (factories.size() == 1) {
				key = descriptions.keySet().iterator().next();
			} else {
				key = "ALL";
			}
		}

		return key;
	}

	
	public <R,D> R getReduction(ReductionTarget<R,D> type,  RecordSelector selector) throws Exception {
		R result = null;
		boolean use_composite = DefaultAccountingService.DEFAULT_COMPOSITE_FEATURE.isEnabled(getContext()) || factories.size() > 1;
		for (UsageProducer<UR> prod:  factories.values()) {
			boolean old = prod.setCompositeHint(use_composite);
			try(TimeClosable t= new TimeClosable(conn, ()->getTag()+"getReduction."+prod.getTag())){
				if( prod.compatible(type.getExpression()) && prod.compatible(selector)){
					if (result == null) {
						result = prod.getReduction(type,  selector);
					} else {
						result = type.combine(result, prod.getReduction(type, 
								selector));
					}
				}
			}finally {
				prod.setCompositeHint(old);
			}
		}
		return result;
	}	

	
	
	public <X> boolean hasProperty(PropertyTag<X> p) {
		// return true if any of the inner producers might have the property
		for (UsageProducer<UR> prod : factories.values()) {
			if ( prod.hasProperty(p)) {
				return true;
			}
		}
		return false;
	}


	public boolean hasProducers(){
		return ! factories.isEmpty();
	}
	public UsageProducer parseProducer(String key) {
		if (key != null) {
			if (key.equals("ALL")) {
	
				return this;
			}
			UsageProducer res = factories.get(key);
			if (res != null) {
				return res;
			}
		}
		// default handling for null or no match
		if (factories.values().size() == 1) {
			return factories.values().iterator().next();
		}
	
		return this;
	}


	/** populate the factories and description table
	 * 
	 */
	protected abstract void populate(String tag);
	/** method to be called by the populate method to register an additional Producer
	 * 
	 * @param description
	 * @param producer
	 */
    protected void addProducer(String description, UsageProducer<UR> producer){
    	factories.put(producer.getTag(), producer);
    	descriptions.put(producer.getTag(),description);
    }
	public PropertyFinder getFinder() {
		MultiFinder finder = new MultiFinder();
		for( UsageProducer up : factories.values()){
			finder.addFinder(up.getFinder());
		}
		return finder;
	}
	public String getImplemenationInfo(PropertyTag<?> tag) {
		StringBuilder sb = new StringBuilder();
		boolean seen=false;
		for(UsageProducer<UR> prod: factories.values()){
			if( prod instanceof PropertyImplementationProvider){
				if( seen ){
					sb.append(" , ");
				}
				sb.append(((PropertyImplementationProvider)prod).getImplemenationInfo(tag));
				seen=true;
			}
		}
		return sb.toString();
	}
	
	@Override
	public String toString(){
		return getClass().getCanonicalName()+"["+tag+"]";
	}

	

	public final PropExpressionMap getDerivedProperties() {
	
		return map;
	}

	public final boolean compatible(RecordSelector sel) {
		for (UsageProducer<UR> prod : factories.values()) {
			if( prod.compatible(sel)){
				return true;
			}
		}
		return false;
	}
	
	
	public final <I> boolean compatible(PropExpression<I> expr) {
		for (UsageProducer<UR> prod : factories.values()) {
			if( prod.compatible(expr)){
				return true;
			}
		}
		return false;
	}
	
//	public boolean isMyRecord(UsageRecord r){
//		for (UsageProducer<UR> prod : factories.values()) {
//			if( prod.isMyRecord(r)){
//				return true;
//			}
//		}
//		return false;
//	}
//	public String getUniqueID(UR r) throws Exception{
//		for (UsageProducer<UR> prod : factories.values()) {
//			if( prod.isMyRecord(r)){
//				return prod.getUniqueID(r);
//			}
//		}
//		return null;
//	}
	private UsageProducer getProducerFromTarget(UR record) {
		for(UsageProducer prod : factories.values()) {
			if( prod.isMyTarget(record)) {
				return prod;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.UsageProducer#getExpressionTarget(java.lang.Object)
	 */
	@Override
	public ExpressionTargetContainer getExpressionTarget(UR record) {
		return getProducerFromTarget(record).getExpressionTarget(record);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.UsageProducer#isMyTarget(java.lang.Object)
	 */
	@Override
	public boolean isMyTarget(UR record) {
		for(UsageProducer prod : factories.values()) {
			if( prod.isMyTarget(record)) {
				return true;
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.UsageProducer#getExpressionIterator(uk.ac.ed.epcc.safe.accounting.selector.RecordSelector)
	 */
	@Override
	public CloseableIterator<ExpressionTargetContainer> getExpressionIterator(RecordSelector sel) throws Exception {
		/*
		 * Generate a Nested Iterator over the combined results of all
		 * Factories.
		 */
		NestedIterator<ExpressionTargetContainer> res = new NestedIterator<>();
		for (UsageProducer<UR> prod: factories.values()) {
			if( prod.compatible(sel)){
				//log.debug("Using "+prod.toString());
				res.add(prod.getExpressionIterator(sel));
			}else{
				getLogger().debug("getIterator: selector not compatible with "+prod.toString());
			}
		}
		return res;
	}
	@Override
	public UsageProducer<UR> narrow(RecordSelector sel) throws Exception {
		try(TimeClosable time = new TimeClosable(conn, () -> getTag()+".narrow")){
			if( factories.size() == 1) {
				// No potential to narrow so just test with compatible
				UsageProducer<UR> prod = factories.values().iterator().next();
				if( prod.compatible(sel)) {
					return prod;
				}
				return null;
			}
			NarrowTag tag = new NarrowTag(getTag(), sel);
			if( getContext().hasAttribute(tag)) {
				return (UsageProducer<UR>) getContext().getAttribute(tag);
			}
			Set<UsageProducer<UR>> set = new LinkedHashSet<>();
			for(UsageProducer<UR> prod :factories.values()) {
				if( prod.compatible(sel) &&  prod.exists(sel)) {
					set.add(prod);
				}
			}
			UsageProducer<UR> res=null;
			if( set.isEmpty()) {
				res=null;
			}else if( set.size()==1) {
				res = set.iterator().next();
			}else if( set.size() == factories.size()) {
				res = this;
			}else {
				res = new ListUsageManager<>(getContext(), getTag()+".narrowed", set);
			}
			getContext().setAttribute(tag, res);
			return res;
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UsageManager other = (UsageManager) obj;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}
	@Override
	public boolean setCompositeHint(boolean composite) {
		// not relevant to UsageManager
		return false;
	}
	
}