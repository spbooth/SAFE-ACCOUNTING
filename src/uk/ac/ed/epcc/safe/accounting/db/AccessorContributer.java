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
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.webapp.jdbc.expr.Accessor;
import uk.ac.ed.epcc.webapp.model.data.Composite;
import uk.ac.ed.epcc.webapp.model.data.DataObject;

/** Interface for {@link Composite}s that add properties
 * 
 * 
 * @see ExpressionTargetFactoryComposite
 * @author spb
 *
 */
public interface AccessorContributer<P extends DataObject> {
	/** Modifies the property config. 
	 * {@link PropertyRegistry}s can be added to the {@link MultiFinder}.
	 * Custom {@link Accessor}s etc. can be added to the {@link AccessorMap}.
	 * Additional derived properties can be defined.
	 * 
	 * This is called before {@link RepositoryAccessorMap#populate(uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder, PropertyRegistry, boolean)}
	 * 
	 * @param mapi2
	 * @param finder
	 * @param derived
	 */
	public  void customAccessors(AccessorMap<P> mapi2,
			MultiFinder finder, PropExpressionMap derived);
}