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
import uk.ac.ed.epcc.safe.accounting.expr.ParseException;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionInput;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionTarget;
import uk.ac.ed.epcc.webapp.jdbc.table.TransitionSource;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
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
 *  
 * 
 * @author spb
 *
 */


public class DerivedPropertyPolicy extends BasePolicy implements TransitionSource<TableTransitionTarget>{
	private PropertyRegistry reg=null;
	private PropExpressionMap defs=new PropExpressionMap();	
	private AppContext c;
	private String table;
	private PropertyFinder finder;
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		this.c=ctx;
		this.table=table;
		this.finder=prev.copy();
		if( reg == null ){
			reg = new PropertyRegistry(table+"derived", "The derived accounting properties for "+table);
			defs.addFromProperties(reg, prev, ctx, table);
		}
		return reg;
	}
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous){
		previous.getAllFrom(defs);
	    return previous;
	}
	public class AddDerivedTransition extends AbstractFormTransition<TableTransitionTarget>{

		public final class AddDerivedAction extends FormAction {
			private final TableTransitionTarget target;
			public AddDerivedAction(TableTransitionTarget target){
				this.target=target;
			}
			@Override
			public FormResult action(Form f)
					throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
				try {
					defs.addConfigProperty(c, reg,finder, table, (String)f.get("Name"), (String)f.get("Expr"));
				} catch (ParseException e) {
					throw new ActionException("Operation failed",e);
				}
				return new ViewTableResult(target);
			}
		}

		public void buildForm(Form f, TableTransitionTarget target,
				AppContext ctx) throws TransitionException {
			f.addInput("Name", "Name of new property", new TextInput(false));
			f.addInput("Expr", "Definition", new PropExpressionInput(c,finder));
			f.addAction("Add", new AddDerivedAction(target));
		}
	}
	public Map<TableTransitionKey<TableTransitionTarget>, Transition<TableTransitionTarget>> getTransitions() {
		Map<TableTransitionKey<TableTransitionTarget>,Transition<TableTransitionTarget>> result = new HashMap<TableTransitionKey<TableTransitionTarget>, Transition<TableTransitionTarget>>();
		// add transitions here
		result.put(new AdminOperationKey<TableTransitionTarget>(TableTransitionTarget.class, "AddDerivedProperty"),new AddDerivedTransition());
		return result;
	}

}