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

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.LinkManager;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;

/** A LinkManager that also supports properties.
 * 
 * @author spb
 *
 * @param <T>
 * @param <L>
 * @param <R>
 */
public abstract class AccountingLinkManager<T extends AccountingLinkManager.PropertyTargetLink<L,R>,L extends DataObject,R extends DataObject> extends LinkManager<T, L, R> 
implements  AccessorContributer<T>{
	

	private ExpressionTargetFactoryComposite<T> etf = new ExpressionTargetFactoryComposite<>(this);

	protected AccountingLinkManager(AppContext c, String table,
			DataObjectFactory<L> left_fac, String left_field,
			DataObjectFactory<R> right_fac, String right_field) {
		super();
		setContext(c, table, left_fac, left_field, right_fac, right_field);
	}
	

public abstract static class PropertyTargetLink<L extends DataObject, R extends DataObject> extends LinkManager.Link<L,R> {
		
		@SuppressWarnings("unchecked")
		protected PropertyTargetLink(AccountingLinkManager<?,L,R> man, Record res) {
			super(man, res);
			
		}
	}




/** Extension point to allow custom accessors and registries to be added.
 * 
 * @param mapi2
 * @param finder
 * @param derived
 */
public void customAccessors(AccessorMap<T> mapi2, MultiFinder finder,
		PropExpressionMap derived) {
	
}



	@Override
	public Class<T> getTarget() {
		return (Class<T>) PropertyTargetLink.class;
	}

	
	
}