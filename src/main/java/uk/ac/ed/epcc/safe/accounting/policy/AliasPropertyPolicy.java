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

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionInput;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyTagInput;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.BaseParser;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;

/**
 * <p>
 * This policy establishes aliases for properties already in scope. The alias
 * can be as simple as one property is equal to another, or can involve
 * complicated property expressions . This is under the control of java
 * properties of the form
 * </p>
 * <blockquote> alias.<em>table-name</em>.
 * <em>name</em>=<em>prop-expression</em> </blockquote>
 * <p>
 * Where <em>name</em> must be the name of a property currently in scope. The
 * scope is defined as the properties that can be found in the
 * <code>PropertyFinder</code> provided during initialisation (in the
 * {@link AliasPropertyPolicy#initFinder} method) as well as all properties
 * present in the <code>base</code> and <code>batch</code> property registers
 * located in {@link BaseParser} and {@link BatchParser} respectively.
 * </p>
 * <p>
 * Note that this allows the definition to be different for different accounting
 * tables. Care needs to be taken to ensure that these different definitions are
 * compatible if a UsageManager is going to be used.
 * </p>
 * 
 * @author jgreen4
 * 
 */


public class AliasPropertyPolicy extends BasePolicy implements TableTransitionContributor {

	/*
	 * TODO consider merging this class with DerivedPropertyPolicy - there
	 * function is almost identical. Merging would be easy - search for a property
	 * tag with the specified name in the finder provided during initialisation.
	 * If it's there, we're making an alias. If it's not, we're making a
	 * derivation.
	 */

	public AliasPropertyPolicy(AppContext conn) {
		super(conn);
	}
	
	private PropExpressionMap aliases = new PropExpressionMap();
    private PropertyFinder cached_finder;
	private String table;
	
	@SuppressWarnings("unchecked")
	@Override
	public PropertyFinder initFinder(PropertyFinder origFinder,
			String table) {
		AppContext ctx = getContext();
		MultiFinder finder = new MultiFinder();
		finder.addFinder(origFinder);
		finder.addFinder(StandardProperties.time);
		finder.addFinder(StandardProperties.base);
		finder.addFinder(BatchParser.batch);

		this.aliases.addAliasesFromProperties(ctx, finder, table);
		
		cached_finder=finder.copy();
		this.table=table;
		return finder;
	}

	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		previous.getAllFrom(aliases);
		return previous;
	}
	public class AddDerivedTransition extends AbstractFormTransition<DataObjectFactory>{

		private static final String EXPR_INPUT = "Expr";
		private static final String PROPERTY_INPUT = "Property";

		public final class AddDerivedAction extends FormAction {
			private final DataObjectFactory target;
			public AddDerivedAction(DataObjectFactory target){
				this.target=target;
			}
			@Override
			public FormResult action(Form f)
					throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {

				PropertyTag tag = (PropertyTag) f.getItem(PROPERTY_INPUT);
				ConfigService cfg = getContext().getService(ConfigService.class);
				cfg.setProperty(PropExpressionMap.ALIAS_PREFIX+table+"."+tag.getFullName(),(String) f.get(EXPR_INPUT));


				return new ViewTableResult(target);
			}
		}

		public void buildForm(Form f, DataObjectFactory target,
				AppContext ctx) throws TransitionException {
			f.addInput(PROPERTY_INPUT, "Property to define", new PropertyTagInput(cached_finder));
			f.addInput(EXPR_INPUT, "Definition", new PropExpressionInput(getContext(),cached_finder));
			f.addAction("Add", new AddDerivedAction(target));
		}
	}
	public Map<TableTransitionKey, Transition> getTableTransitions() {
		Map<TableTransitionKey,Transition> result = new HashMap<>();
		// add transitions here
		result.put(new AdminOperationKey( "AddDerivedProperty"),new AddDerivedTransition());
		return result;
	}

}