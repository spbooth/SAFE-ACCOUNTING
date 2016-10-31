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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.HtmlBuilder;
import uk.ac.ed.epcc.webapp.forms.exceptions.FieldException;
import uk.ac.ed.epcc.webapp.forms.exceptions.MissingFieldException;
import uk.ac.ed.epcc.webapp.forms.factory.FormUpdateProducer;
import uk.ac.ed.epcc.webapp.forms.html.EmitHtmlInputVisitor;
import uk.ac.ed.epcc.webapp.forms.inputs.InputVisitor;
import uk.ac.ed.epcc.webapp.forms.inputs.ListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TypeError;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.forms.Selector;
import uk.ac.ed.epcc.webapp.model.data.iterator.AbstractMultiIterator;
import uk.ac.ed.epcc.webapp.model.data.iterator.NestedIterator;
/** A composite {@link UsageProducer} that combines results from several 
 * underlying {@link UsageProducer}s.
 * 
 * @author spb
 *
 * @param <UR>
 */

public abstract class UsageManager<UR extends UsageRecord> implements
		UsageProducer<UR> , Selector<ListInput<String, UsageProducer>>, 
		DerivedPropertyFactory,FormUpdateProducer<UR>{

	/**
	 * FormInput for selecting UsageProducers.
	 * 
	 * Actually we may not need this as the ProducerTable input might be fine.
	 * 
	 * @author spb
	 * 
	 */


	public class ProducerInput implements ListInput<String, UsageProducer> {
		String value = null;
		String key = null;
		boolean allow_all = false;
		

		public ProducerInput(boolean allow_all) {
			this.allow_all = allow_all;
		}

		public String convert(Object v) throws TypeError {
			return (String) v;
		}

		public UsageProducer getItem() {
			return getItembyValue(value);
		}

		public UsageProducer getItembyValue(String value) {
			return parseProducer(value);
		}

		public Iterator<UsageProducer> getItems() {
			LinkedList<UsageProducer> list = new LinkedList<UsageProducer>();
			if (allow_all) {
				list.add(UsageManager.this);
			}
			for (String key : factories.keySet()) {
				list.add(factories.get(key));
			}
			return list.iterator();
		}
		public int getCount() {
			int count=0;
			if (allow_all) {
				count++;
			}
			for (String key : factories.keySet()) {
				count++;
			}
			return count;
		}

		public String getKey() {
			return key;
		}

		public String getPrettyString(String value) {
			if( value == null || value.equals("ALL")){
				return "ALL";
			}
			return descriptions.get(value);
		}

		public String getString(String value) {
			return value;
		}

		public String getTagByItem(UsageProducer item) {
			if (item == UsageManager.this) {
				return "ALL";
			}
			return item.getTag();
		}

		public String getTagByValue(String value) {
			return value;
		}

		public String getText(UsageProducer item) {
			return getPrettyString(getTagByItem(item));
		}

		public String getValue() {
			return value;
		}

		public void setItem(UsageProducer item) {
			value = getTagByItem(item);
		}

		public void setKey(String key) {
			this.key = key;

		}

		public String setValue(String v) throws TypeError {
			String old = value;
			value = v;
			return old;
		}

		public void validate() throws FieldException {
			if (value == null) {
				throw new MissingFieldException();
			}
		}

		public <R> R accept(InputVisitor<R> vis) throws Exception {
			return vis.visitListInput(this);
		}

		@Override
		public boolean isValid(UsageProducer item) {
			if( item.equals(UsageManager.this)){
				return allow_all;
			}
			return factories.containsValue(item);
		}

	}
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
				log.error("Error in MultiIterator",e);
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
	

	
   
    private AppContext ctx;
	protected final Logger log;
	/**
	 * Stores the underlying implementation classes all should Implement
	 * AccountingProducer
	 */
	private  LinkedHashMap<String, UsageProducer<UR>> factories;
	private final PropExpressionMap map; 
	private  LinkedHashMap<String,String> descriptions;
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
		super();
		this.tag=tag;
		ctx = c;
		log=c.getService(LoggerService.class).getLogger(getClass());
		// cache the factories list in the AppContext as there is a
		// high probablity of reuse within a request and this hardly ever
		// changes
		String key = "AccountingManager.factories."+tag;
		String desc_key = "AccountingManager.descriptions."+tag;
		factories = (LinkedHashMap<String, UsageProducer<UR>>) ctx.getAttribute(key);
		descriptions = (LinkedHashMap<String,String>) ctx.getAttribute(desc_key);
		if (factories == null) {
			// want defined iteration order
			factories = new LinkedHashMap<String,UsageProducer<UR>>();
			descriptions =new LinkedHashMap<String,String>();
			
			populate(tag);
			
			ctx.setAttribute(key, factories);
			ctx.setAttribute(desc_key, descriptions);
		}
		PropExpressionMap tmp = null;
		for( UsageProducer<UR> prod : factories.values()){
			if( prod instanceof DerivedPropertyFactory){
				if( tmp == null ){
					// initial set from first seen
					// make sure we copy as this map is edited.
					// nested SHOULD return a copy but lets be safe as this has bitten us before
					tmp = new PropExpressionMap(((DerivedPropertyFactory) prod).getDerivedProperties()); 
				}else{
					// merge definitions we only want the set common to all producers.
				
					PropExpressionMap merge = ((DerivedPropertyFactory) prod).getDerivedProperties(); 
					Iterator<PropertyTag> it = tmp.keySet().iterator();
					while(it.hasNext()){
						PropertyTag t = it.next();
						if( ! merge.containsKey(t) || ! tmp.get(t).equals(merge.get(t))){
							it.remove();
						}
					}
				}
			}else{
				// must return empty set
				if( tmp == null ){
					tmp = new PropExpressionMap();
				}else{
					tmp.clear();
				}
				break;
			}
		}
		map=tmp;
	}

	

	/**
	 * get the AppContext
	 * 
	 * @return AppContext
	 */
	public AppContext getContext() {
		return ctx;
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
			try {
				if( prod.compatible(sel)){
					result += prod.getRecordCount(sel);
				}
			} catch (Exception e) {
				log.error("Error in getCount",e);
			}

		}
		return result;
	}
	public  Iterator<UR> getIterator( RecordSelector sel) throws Exception {
		/*
		 * Generate a Nested Iterator over the combined results of all
		 * Factories.
		 */
		NestedIterator<UR> res = new NestedIterator<UR>();
		for (UsageProducer<UR> prod: factories.values()) {
			if( prod.compatible(sel)){
				//log.debug("Using "+prod.toString());
				res.add(prod.getIterator(sel));
			}else{
				log.debug("getIterator: selector not compatible with "+prod.toString());
			}
		}
		return res;
	}
	public  Iterator<UR> getIterator( RecordSelector sel,int skip, int count) throws DataFault {
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


	public <R> Map<R, Number> getReductionMap(
			PropExpression<R> tag,ReductionTarget<Number> res, 
			 RecordSelector selector) 
			throws Exception {
		Map<R, Number> result = null;
		
			for (UsageProducer<UR> prod: factories.values()) {
				if( prod.compatible(tag) && prod.compatible(selector)){
					if (result == null) {
						result = prod.getReductionMap(tag, res, selector);
					} else {
						Map<R, Number> merge = prod.getReductionMap(tag, res, selector);
						for (R key : merge.keySet()) {
							result.put(key, res.combine(result.get(key),merge
									.get(key)));
						}
					}
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
		//TODO change avg to sum and count
		for(UsageProducer prod : factories.values()){
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
		}
		if( result == null){
			return new HashMap<ExpressionTuple, ReductionMapResult>();
		}
		return result;
	}

	

	


	public String getProducerSelector(String name, String key){
		return getProducerSelector(name, key,true);
	}
	
	public String getProducerSelector(String name, String key,boolean allow_all){
		ListInput<String, UsageProducer> input = getProducerInput(allow_all);
		input.setKey(name);
		
		Map<String,String> params = new HashMap<String,String>();
		params.put(name, getProducerTag(key));
		try {
			HtmlBuilder hb = new HtmlBuilder();
			EmitHtmlInputVisitor vis = new EmitHtmlInputVisitor(getContext(),hb, true, params, null);
			input.accept(vis);
			return hb.toString();
			
		} catch (Exception e) {
			log.error("Error making selctor from input",e);
			return null;
		}
	}



	public ListInput<String,UsageProducer> getProducerInput(boolean allow_all) {
		return new ProducerInput(allow_all);
	}
	@SuppressWarnings("unchecked")
	public <C extends UsageProducer> Vector<C> getProducers(Class<C> target) {
		Vector<C> res = new Vector<C>();
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

	
	public <R> R getReduction(ReductionTarget<R> type,  RecordSelector selector) throws Exception {
		R result = null;
		for (UsageProducer<UR> prod:  factories.values()) {
			if( prod.compatible(type.getExpression()) && prod.compatible(selector)){
				if (result == null) {
					result = prod.getReduction(type,  selector);
				} else {
					result = type.combine(result, prod.getReduction(type, 
							selector));
				}
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
			if( seen ){
				sb.append(" , ");
			}
			sb.append(prod.getImplemenationInfo(tag));
			seen=true;
		}
		return sb.toString();
	}
	public ListInput<String,UsageProducer> getInput() {
		return getProducerInput(true);
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
	protected Logger getLogger(){
		return log;
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
}