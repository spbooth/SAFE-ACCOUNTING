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
package uk.ac.ed.epcc.safe.accounting.policy;

import java.util.HashMap;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.UsageManager;
import uk.ac.ed.epcc.safe.accounting.db.ExpressionTargetFactoryComposite;
import uk.ac.ed.epcc.safe.accounting.expr.AddDerivedTransition;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
/** This policy generates new derived properties 
 * which are defined as expressions over other properties that
 * are already in scope.
 * The new properties are created in a PropertyRegistry called <em>table-name</em>derived.
 * 
 * This is under the control of java properties of the form
 * <b>
 * properties.<em>table-name</em>.name=<em>prop-expression</em>
 * </b>
 * 
 * Note that the registry is bound to the table so these properties will not be
 * available in a {@link UsageManager} that includes the table. To define derived properties 
 * that are visible across a {@link UsageManager} a {@link ExpressionPropertyPolicy} is required.
 * <p>
 * This policy is now largely redundant due to the built in support for derived properties
 * in {@link ExpressionTargetFactoryComposite}. 
 * 
 * @author spb
 * @see ExpressionTargetFactoryComposite
 *
 */


public class DerivedPropertyPolicy extends BasePolicy implements TableTransitionContributor{
	public DerivedPropertyPolicy(AppContext conn) {
		super(conn);
	}
	private PropertyRegistry reg=null;
	private PropExpressionMap defs=new PropExpressionMap();	
	
	private String table;
	private PropertyFinder finder;
	@Override
	public PropertyFinder initFinder(PropertyFinder prev,
			String table) {
		
		this.table=table;
		this.finder=prev.copy();
		if( reg == null ){
			reg = new PropertyRegistry(table+"derived", "The derived accounting properties for "+table);
			defs.addFromProperties(reg, prev, getContext(), table);
		}
		return reg;
	}
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous){
		previous.getAllFrom(defs);
	    return previous;
	}
	public Map<TableTransitionKey, Transition> getTableTransitions() {
		Map<TableTransitionKey,Transition> result = new HashMap<>();
		// add transitions here
		result.put(new AdminOperationKey("AddDerivedProperty"),new AddDerivedTransition(defs,reg,finder,table));
		return result;
	}

}