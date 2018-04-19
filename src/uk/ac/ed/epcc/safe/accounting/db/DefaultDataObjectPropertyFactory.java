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
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.table.TableStructureListener;
/** Default implementation of the DataObjectPropertyFactory logic.
 * 
 * To keep this lightweight when this functionality is not in use the 
 * {@link AccessorMap} is made lazily.
 * @author spb
 *
 * @param <T>
 */
public abstract  class DefaultDataObjectPropertyFactory<T extends DataObjectPropertyContainer> extends
		DataObjectPropertyFactory<T> implements UsageProducer<T>, TableStructureListener{
	private PropertyFinder reg=null;
	private RepositoryAccessorMap<T> map=null;
	private PropExpressionMap expression_map=null;
	
	protected final void initAccessorMap(AppContext c, String table) {
		map = new RepositoryAccessorMap<T>(this,res);
		MultiFinder finder = new MultiFinder();
		ReferencePropertyRegistry refs = ReferencePropertyRegistry.getInstance(c);
		map.makeReferences(refs);
		finder.addFinder(refs);
		PropertyRegistry derived = new PropertyRegistry(table+"DerivedProperties","Derived properties for table "+table);
		expression_map = new PropExpressionMap();
		PropertyRegistry def = new PropertyRegistry(table,"Properties for table "+table);

		for(AccessorContributer contrib : getComposites(AccessorContributer.class)){
			contrib.customAccessors(map, finder, expression_map);
		}
		customAccessors(map, finder, expression_map);
		map.populate( finder, def,false);
		finder.addFinder(def);
		
		
		expression_map.addFromProperties(derived, finder, c, table);
		map.addDerived(c, expression_map);
		finder.addFinder(derived);
		
		reg=finder;
	}
	/** Extension point to allow custom accessors and registries to be added.
	 * 
	 * @param mapi2
	 * @param finder
	 * @param derived
	 */
	protected void customAccessors(AccessorMap<T> mapi2, MultiFinder finder,
			PropExpressionMap derived) {
		
	}
	
	
	public final PropertyFinder getFinder() {
		if( reg == null ){
			initAccessorMap(getContext(), getTag());
		}
		return reg;
	}

	
	


	

	public void resetStructure() {
		initAccessorMap(getContext(), getConfigTag());
	}

	public final RepositoryAccessorMap<T> getAccessorMap() {
		if( map == null ){
			initAccessorMap(getContext(), getTag());
		}
		return map;
	}



	
	public final PropExpressionMap getDerivedProperties() {
		if( expression_map == null){
			initAccessorMap(getContext(), getTag());
		}
		return expression_map;
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
	private ReductionHandler<T, DefaultDataObjectPropertyFactory<T>> getReductionHandler(){
		return new ReductionHandler<T, DefaultDataObjectPropertyFactory<T>>(this);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.ReductionProducer#getReduction(uk.ac.ed.epcc.safe.accounting.ReductionTarget, uk.ac.ed.epcc.safe.accounting.selector.RecordSelector)
	 */
	@Override
	final public <T> T getReduction(ReductionTarget<T> target, RecordSelector sel) throws Exception {
		return getReductionHandler().getReduction(target, sel);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.ReductionProducer#getIndexedReductionMap(java.util.Set, uk.ac.ed.epcc.safe.accounting.selector.RecordSelector)
	 */
	@Override
	final public Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap(Set<ReductionTarget> property,
			RecordSelector selector) throws Exception {
		return getReductionHandler().getIndexedReductionMap(property, selector);
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.ReductionProducer#getReductionMap(uk.ac.ed.epcc.safe.accounting.properties.PropExpression, uk.ac.ed.epcc.safe.accounting.ReductionTarget, uk.ac.ed.epcc.safe.accounting.selector.RecordSelector)
	 */
	@Override
	final public <I> Map<I, Number> getReductionMap(PropExpression<I> index, ReductionTarget<Number> property,
			RecordSelector selector) throws Exception {
		return getReductionHandler().getReductionMap(index, property, selector);
	}
	
	
	
	
}