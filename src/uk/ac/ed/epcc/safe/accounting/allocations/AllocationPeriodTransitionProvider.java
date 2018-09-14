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

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.FilterSelectVisitor;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.formatters.value.ShortTextPeriodFormatter;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.CalendarFieldPeriodInput;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
import uk.ac.ed.epcc.webapp.forms.inputs.OptionalInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.ViewTransitionResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractDirectTargetlessTransition;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractDirectTransition;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.IndexTransitionFactory;
import uk.ac.ed.epcc.webapp.forms.transition.PathTransitionProvider;
import uk.ac.ed.epcc.webapp.forms.transition.TransitionFactoryCreator;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.forms.Selector;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.data.transition.AbstractViewPathTransitionProvider;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** Provide a filtered view of allocations from a nested
 * {@link AllocationManager}. Transitions only implement changes to the filter
 * including changes of time period. The changes to the records themselves is handled by
 * the {@link AllocationManager}
 * 
 * @author spb
 * @param <T> type of {@link Allocation}
 * @param <K> transition key for {@link AllocationManager}
 *
 */
public class AllocationPeriodTransitionProvider<T extends DataObject&Allocation,K> extends
		AbstractViewPathTransitionProvider<AllocationPeriod, PeriodKey>implements PathTransitionProvider<PeriodKey,AllocationPeriod>,
		IndexTransitionFactory<PeriodKey, AllocationPeriod>{

	public static final String ALLOCATION_PERIOD_PREFIX = "AllocationPeriod";

	/** Relationship with index property targets that (if defined) will restrict the selcted
	 * index properties and narrow the index view
	 * 
	 */
	public static final String ALLOCATION_ADMIN_RELATIONSHIP = "AllocationAdmin";


	public static PeriodKey UP_KEY = new PeriodKey(">>>", "Go to next period");
	public class UpTransition extends AbstractDirectTransition<AllocationPeriod>{

		public FormResult doTransition(AllocationPeriod target, AppContext c)
				throws TransitionException {
			
			return new ViewTransitionResult<AllocationPeriod, PeriodKey>(AllocationPeriodTransitionProvider.this, new AllocationPeriod(target.getPeriod().up(), target.getIndex()));
		}
		
	}
	public static PeriodKey DOWN_KEY = new PeriodKey("<<<", "Go to previous period");
	public class DownTransition extends AbstractDirectTransition<AllocationPeriod>{
		public FormResult doTransition(AllocationPeriod target, AppContext c)
				throws TransitionException {
			
			return new ViewTransitionResult<AllocationPeriod, PeriodKey>(AllocationPeriodTransitionProvider.this, new AllocationPeriod(target.getPeriod().down(), target.getIndex()));
		}
		
	}
	
	public static final PeriodKey FILTER_KEY = new PeriodKey("Filter", "Change view filter");
	private static final String FILTER_FORM_PREFIX = "filter.";
	public class FilterAction extends FormAction{


		@Override
		public FormResult action(Form f) throws ActionException {
			CalendarFieldPeriodInput input = (CalendarFieldPeriodInput) f.getInput("Period");
			ViewPeriod period = new ViewPeriod(input.getValue());
			PropertyMap map = new PropertyMap();
		
			for(ReferenceTag tag : manager.getIndexProperties()){
				// only filter if actually present
				if( manager.hasProperty(tag)){
	
					IndexedProducer prod = tag.getFactory(getContext());
					if( prod instanceof Selector){
						Integer id = (Integer) f.get(FILTER_FORM_PREFIX+tag.getFullName());
						if( id != null ){
							map.setProperty(tag, tag.makeReference(id.intValue()));
						}
					}					
				}
			}
			return new ViewTransitionResult<AllocationPeriod,PeriodKey>(AllocationPeriodTransitionProvider.this, 
					new AllocationPeriod(period, map));
		}
		
	}
	public class FilterTransition extends AbstractFormTransition<AllocationPeriod>{

		
		public void buildForm(Form f, AllocationPeriod target, AppContext conn)
				throws TransitionException {
			CalendarFieldPeriodInput input = new CalendarFieldPeriodInput(Calendar.MONTH);
			SessionService sess = conn.getService(SessionService.class);
			input.setValue(target.getPeriod());
			f.addInput("Period", "Period", input );
			// If we have any references then allow filter on these.
			PropertyMap map = target.getIndex(); 
			for(ReferenceTag<?,?> tag : manager.getIndexProperties()){
				// only filter if actually present
				if( manager.hasProperty(tag)){
	
					IndexedProducer prod = tag.getFactory(conn);
					if( prod instanceof Selector){
						Selector sel = (Selector)  prod;
						Input<Integer> i=null; 
						if( prod instanceof DataObjectFactory){
							DataObjectFactory fac = (DataObjectFactory) prod;
							i = fac.getInput(sess.getRelationshipRoleFilter(fac, ALLOCATION_ADMIN_RELATIONSHIP,fac.getFinalSelectFilter()));
						}
						if( i == null){
							i  = (Input<Integer>)sel.getInput();
						}
						if( i instanceof OptionalInput){
							((OptionalInput)i).setOptional(true);
						}
						IndexedReference ref = map.getProperty(tag, null);
						if( ref != null){
							i.setValue(ref.getID());
						}
						// don't use same names as in target path
						f.addInput(FILTER_FORM_PREFIX+tag.getFullName(), tag.getTable(), i);
					}
				}
			}
			f.addAction("Filter", new FilterAction());
		}
	}
	public static PeriodKey INDEX_KEY = new PeriodKey("Index"){
		@Override
		public boolean allow(SessionService sess,AllocationPeriod target){
			return target == null;
		}
	};
	public class IndexTransition extends AbstractDirectTargetlessTransition<AllocationPeriod>{
		public FormResult doTransition(AppContext c) throws TransitionException {
			return new ViewTransitionResult<AllocationPeriod, PeriodKey>(AllocationPeriodTransitionProvider.this, new AllocationPeriod(ViewPeriod.getViewPeriod(c)));
		}
		
	}
	public static PeriodKey CREATE_KEY = new PeriodKey("Create", "Create a new allocation"){

		/* (non-Javadoc)
		 * @see uk.ac.ed.epcc.safe.accounting.allocations.PeriodKey#allow(uk.ac.ed.epcc.webapp.session.SessionService, uk.ac.ed.epcc.safe.accounting.allocations.AllocationPeriod)
		 */
		@Override
		public boolean allow(SessionService sess, AllocationPeriod target) {
			return sess.hasRole(AllocationFactory.ALLOCATION_ADMIN_ROLE);
		}
		
	};
	public class CreateTransition extends AbstractFormTransition<AllocationPeriod>{

		public void buildForm(Form f, AllocationPeriod target, AppContext conn)
				throws TransitionException {
			manager.buildCreationForm(f, target.getPeriod(), target.getIndex());
			
		}
	}
	public AllocationPeriodTransitionProvider(AllocationManager<K, T> manager) {
		super(manager.getContext());
		this.manager=manager;
		addTransition(DOWN_KEY, new DownTransition());
		addTransition(FILTER_KEY, new FilterTransition());
		addTransition(CREATE_KEY, new CreateTransition());
		addTransition(INDEX_KEY, new IndexTransition());
		addTransition(UP_KEY, new UpTransition());
	}

	protected final AllocationManager<K, T> manager;

	

	public String getTargetName() {
		return ALLOCATION_PERIOD_PREFIX+TransitionFactoryCreator.TYPE_SEPERATOR+manager.getTag();
	}

	public boolean allowTransition(AppContext c, AllocationPeriod target,
			PeriodKey key) {
		SessionService sess = c.getService(SessionService.class);
		return canView(target, sess) && key.allow(sess,target);
	}

	public <X extends ContentBuilder> X getSummaryContent(AppContext c, X cb,
			AllocationPeriod target) {
		ShortTextPeriodFormatter fmt = new ShortTextPeriodFormatter();
		
		cb.addHeading(3, "Period: "+fmt.format(target.getPeriod()));
		PropertyMap map = target.getIndex();
		Table t = new Table();
		for(ReferenceTag<?,?> tag : manager.getIndexProperties()){
			IndexedReference ref = map.getProperty(tag, null);
			if( ref != null ){
				Labeller labeller = tag.getLabeller();
				if( labeller != null ){
					t.put("Value", tag.getTable(), labeller.getLabel(c, ref));
				}else{
					// This won't look good but ReferenceTags should provide non-null labeller
					t.put("Value", tag.getTable(), ref);
				}
			}
			t.setKeyName("Filter");
			
		}
		if( t.hasData()){
			cb.addColumn(c, t, "Value");
		}
		return cb;
	}

	@Override
	public <X extends ContentBuilder> X getLogContent(X cb,
			AllocationPeriod target, SessionService<?> sess) {
		AppContext c = getContext();
		getSummaryContent(c, cb, target);
		try{
			Table tab = new Table();

			for(Iterator<T> it = getIndexIterator(target);it.hasNext();){
				T record = it.next();
				manager.addIndexTable(tab, record,target);
			}
			manager.finishIndexTable(tab, target);
			if( tab.hasData()) {
				cb.addText("Dates in red indicate records that cross the time period of the filter. Grey End-dates indicate an allocation that is past");
				cb.addTable(c, tab);
			}
		}catch(Exception e){
			getLogger().error("Error making index table",e);
		}
		return cb;
	}

	/**
	 * @param target
	 * @return
	 * @throws Exception
	 */
	public Iterator<T> getIndexIterator(AllocationPeriod target) throws Exception {
		ExpressionTargetFactory<T> etf = ExpressionCast.getExpressionTargetFactory(manager);
		DataObjectFactory<T> fac = (DataObjectFactory<T>) manager;
		if( etf != null ){
			SessionService sess = getContext().getService(SessionService.class);
			
			AccessorMap map = etf.getAccessorMap();
			AndFilter fil = new AndFilter(fac.getTarget());
			fil.addFilter(getSelector(target).visit(new FilterSelectVisitor<>(etf)));
			fil.addFilter(manager.getViewFilter(sess));

			fil.addFilter(map.getOrderFilter(false,StandardProperties.STARTED_PROP));
			return fac.getResult(fil).iterator();
		}
		return manager.getIterator(getSelector(target));
	}

	public boolean canView(AllocationPeriod target, SessionService<?> sess) {
		return sess.hasRole(AllocationManager.ALLOCATION_ADMIN_ROLE);
	}
	@SuppressWarnings("unchecked")
	public RecordSelector getSelector(AllocationPeriod view){
		AndRecordSelector sel = new AndRecordSelector();
		sel.add(new PeriodOverlapRecordSelector(view.getPeriod(), StandardProperties.STARTED_PROP, StandardProperties.ENDED_PROP));
		PropertyMap map = view.getIndex();
		for(ReferenceTag tag : manager.getIndexProperties()){
			if( manager.hasProperty(tag)){
				IndexedReference ref = (IndexedReference) map.getProperty(tag, null);
				if( ref != null){
					sel.add(new SelectClause<IndexedReference>(tag,ref));
				}
			}
		}
		
		return sel;
	}

	public AllocationPeriod getTarget(LinkedList<String> id) {
		if( id == null || id.size()==0){
			// make a default
			
			return new AllocationPeriod(ViewPeriod.getViewPeriod(getContext()));
		}
		AllocationPeriod result = new AllocationPeriod(ViewPeriod.parsePeriod(id.pop()));
		PropertyFinder finder =manager.getFinder();
		PropertyMap map = result.getIndex();
		Set<ReferenceTag> index_set = manager.getIndexProperties();
		for(String s : id){
			int pos = s.indexOf("=");
			if( pos > 0){
				try{
				  ReferenceTag t = (ReferenceTag) finder.find(s.substring(0, pos));
				  if( t != null && index_set.contains(t)){
					  map.setProperty(t,t.makeReference(Integer.parseInt(s.substring(pos+1))));
					  
				  }
				}catch(Exception t){
					getLogger().warn("Error setting period property", t);
				}
			}
		}
		return result;
	}

	public LinkedList<String> getID(AllocationPeriod target) {
		LinkedList<String> result = new LinkedList<String>();
		result.add(target.getPeriod().toString());
		PropertyMap map = target.getIndex();
		for(ReferenceTag<?,?> tag : manager.getIndexProperties()){
			IndexedReference ref  = map.getProperty(tag, null);
			if( ref != null ){
				result.add(tag.getFullName()+"="+ref.getID());
			}
		}
		return result;
	}

	public PeriodKey getIndexTransition() {
		return INDEX_KEY;
	}

	
}