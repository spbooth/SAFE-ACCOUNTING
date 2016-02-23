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

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.ParseException;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionInput;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.selector.FilterSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FalseFilter;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.DataObjectItemInput;
import uk.ac.ed.epcc.webapp.model.data.transition.TransitionKey;
/** Factory class for {@link AccountingClassification} objects.
 * 
 * By default the properties are generated from the Database fields but additional properties can be
 * defined as derived properties
 * @author spb
 *
 * @param <T>
 */


public class AccountingClassificationFactory<T extends AccountingClassification>
		extends PropertyTargetClassificationFactory<T>  implements UsageProducer<T>,FilterSelector<DataObjectItemInput<T>>{
	private PropertyFinder reg=null;
	private AccessorMap<T> map=null;
	public static final PropertyRegistry classification = new PropertyRegistry("classification", "Standard properties for a Classification table");
	public static final PropertyTag<String> NAME_PROP = new PropertyTag<String>(classification,Classification.NAME,String.class);
	public static final PropertyTag<String> DESCRIPTION_PROP = new PropertyTag<String>(classification,Classification.DESCRIPTION,String.class);
	static{
		classification.lock();
	}
	
	PropertyRegistry derived;
	private PropExpressionMap expression_map=null;

	public AccountingClassificationFactory(AppContext c, String table) {
		super(c, table);
		// Logger log = getLogger();
		
	}
	/** Extension point to allow custom accessors to be added.
	 * 
	 * @param mapi2
	 * @param finder
	 * @param derived
	 */
	protected void customAccessors(AccessorMap<T> mapi2, MultiFinder finder,
			PropExpressionMap derived) {
		
	}
	
	private void initAccessorMap(AppContext c, String tag) {
		map = new AccessorMap<T>(getTarget(),res,tag);
		MultiFinder finder = new MultiFinder();
		ReferencePropertyRegistry refs = ReferencePropertyRegistry.getInstance(c);
		map.makeReferences( refs);
		finder.addFinder(refs);
		finder.addFinder(classification);
		derived = new PropertyRegistry(tag+"DerivedProperties","Derived properties for table "+tag);
		expression_map = new PropExpressionMap();
		
		customAccessors(map, finder, expression_map);
		PropertyRegistry def = new PropertyRegistry(tag,"Properties for table "+tag);
		map.populate( finder, def,false);
		finder.addFinder(def);
		
		finder.addFinder(map.setRelationshipProperties(this));
		
		
		
		
		expression_map.addFromProperties(derived, finder, c, tag);
		map.addDerived(c, expression_map);
		finder.addFinder(derived);
		
		reg=finder;
	}

	public PropertyFinder getFinder() {
		if( reg == null){
			initAccessorMap(getContext(), getConfigTag());
		}
		return reg;
	}

	

	public final AccessorMap<T> getAccessorMap(){
		if( map == null ){
			initAccessorMap(getContext(), getConfigTag());
		}
		return map;
	}

	@Override
	protected PropExpressionMap getDerivedProperties() {
		if( expression_map == null){
			initAccessorMap(getContext(), getConfigTag());
		}
		return expression_map;
		
	};

	@Override
	public void resetStructure() {
		super.resetStructure();
		initAccessorMap(getContext(), getConfigTag());
	}

	public DataObjectItemInput<T> getInput(RecordSelector sel) throws Exception {
		
		return new DataObjectInput(sel.visit(new FilterSelectVisitor<T>(this)));
	}

	
	public <I> BaseFilter<T> getSelectClauseFilter(SelectClause<I> c) {
		try {
			if( c.data == null ){
				// null data implies no match
				return new FalseFilter<T>(getTarget());
			}
			if( c.tag instanceof PropertyTag){
				return getAccessorMap().getFilter((PropertyTag<I>)c.tag, c.match, c.data);
			}else{
				return getAccessorMap().getFilter(c.tag, c.match, c.data);
			}
		} catch (CannotFilterException e) {
			getContext().getService(LoggerService.class).getLogger(getClass()).warn("Cannot filter", e);
			return new FalseFilter<T>(getTarget());
		}
	}
	public class AddDerivedTransition extends AbstractFormTransition<AccountingClassificationFactory>{

		public void buildForm(Form f, AccountingClassificationFactory target,
				AppContext c) throws TransitionException {
			f.addInput("Name", "Name of new property", new TextInput(false));
			f.addInput("Expr", "Definition", new PropExpressionInput(getContext(),getFinder()));
			// Note this class references the enclosing factory directly rather than
			// using the target parameter as this allows it internal access
			f.addAction("Add", new FormAction() {

				@Override
				public FormResult action(Form f)
						throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
					try {
						expression_map.addConfigProperty(getContext(), derived,getFinder(), getConfigTag(), (String)f.get("Name"), (String)f.get("Expr"));
					} catch (ParseException e) {
						throw new ActionException("Operation failed",e);
					}
					return new ViewTableResult(AccountingClassificationFactory.this);
				}
			});
		}		
	}

    public class AccountingClassificationTableRegistry extends PropertyTargetClassificationTableRegistry{

		public AccountingClassificationTableRegistry() {
			addTableTransition(new TransitionKey<AccountingClassificationFactory>(AccountingClassificationFactory.class, "Add derived property"), new AddDerivedTransition());
		}
    	
    }

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.PropertyTargetClassificationFactory#makeTableRegistry()
	 */
	@Override
	protected AccountingClassificationTableRegistry makeTableRegistry() {
		return new AccountingClassificationTableRegistry();
	}
	public String getImplemenationInfo(PropertyTag<?> tag) {
		return getAccessorMap().getImplemenationInfo(tag);
	}
	private ReductionHandler<T, AccountingClassificationFactory<T>> getReductionHandler(){
		return new ReductionHandler<T, AccountingClassificationFactory<T>>(this);
	}

	public <I> Map<I, Number> getReductionMap(PropExpression<I> index,
			ReductionTarget<Number> property,  RecordSelector selector)
			throws Exception 
	{
		return getReductionHandler().getReductionMap(index, property, selector);
		
	}

	
	public  Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap( Set<ReductionTarget> sum, RecordSelector selector) throws Exception{
		return getReductionHandler().getIndexedReductionMap(sum, selector);
	}
	
	
	public  <R>  R getReduction(ReductionTarget<R> type, RecordSelector selector) throws Exception {
		return getReductionHandler().getReduction(type, selector);
	}
	
	@Override
	public void release() {
		if( map != null){
			map.release();
			map=null;
		}
		reg=null;
		super.release();
	}
	
}