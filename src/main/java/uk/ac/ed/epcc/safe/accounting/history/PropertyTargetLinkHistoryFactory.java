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

import uk.ac.ed.epcc.safe.accounting.db.ExpressionTargetFactoryComposite;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.HistoryFactory;
import uk.ac.ed.epcc.webapp.model.data.IndexedLinkManager;
import uk.ac.ed.epcc.webapp.model.history.LinkHistoryManager;



public class PropertyTargetLinkHistoryFactory<L extends Indexed, R extends Indexed, T extends IndexedLinkManager.Link<L,R>,H extends HistoryFactory.HistoryRecord<T>>
extends LinkHistoryManager<L, R, T, H>
{
	private ExpressionTargetFactoryComposite<H> etf = new ExpressionTargetFactoryComposite<>(this);
	private PropertyHistoryComposite<T, IndexedLinkManager<T, L, R>, H> history_prop = new PropertyHistoryComposite<>(this);

	public PropertyTargetLinkHistoryFactory(IndexedLinkManager<T, L, R> fac, String table) {
		super(fac);
		setContext(fac.getContext(), table);
	}

	
}