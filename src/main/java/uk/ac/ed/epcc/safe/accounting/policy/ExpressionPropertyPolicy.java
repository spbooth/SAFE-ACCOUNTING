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

import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionInput;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyTagInput;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ConfigPropertyRegistry;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.PatternTextInput;
import uk.ac.ed.epcc.webapp.forms.inputs.SetInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** This policy allows new properties to be defined as derived property expressions. 
 * It also provides a mechanism for {@link ConfigPropertyRegistry} objects to
 * be included in scope so that entirely new properties can be defined.
 * This is intended to be a replacement for the {@link DerivedPropertyPolicy} so
 * the property definitions are compatible with this and the syntax supported by {@link PropExpressionMap}but it requires additional configuration parameters.
 * <p>
 * The {@link ConfigPropertyRegistry}s added to the {@link PropertyFinder} are specified by
 * <b>
 * registry_list.<em>table-name</em>=name [, name]*
 * </b>
 * This defaults to <b>expression</b>
 * </p>
 * 
 * Property definitions are under the control of java properties of the form
 * <b>
 * properties.<em>table-name</em>.<em>name</em>=<em>prop-expression</em>
 * </b>
 * 
 * @author spb
 *
 */


public class ExpressionPropertyPolicy extends BasePolicy implements TableTransitionContributor, SummaryProvider{
	public ExpressionPropertyPolicy(AppContext conn) {
		super(conn);
	}

	private PropExpressionMap defs=new PropExpressionMap();	
	private String table;
	private MultiFinder finder; // finder to use for expression parse
	private MultiFinder props; // defined props
	@Override
	public PropertyFinder initFinder(PropertyFinder prev,
			String table) {
		
		this.table=table;
		this.finder=new MultiFinder();
		this.finder.addFinder(prev);
		props = new MultiFinder();
		AppContext c = getContext();
		String list = c.getInitParameter("registry_list."+table, "expression");
		for( String name : list.split("\\s*,\\s*")){
			props.addFinder(new ConfigPropertyRegistry(c, name));
		}
		this.finder.addFinder(props);
		// This will look at all definitions for the table
		// not all of which may apply to this policy. It might have been better
		// to qualify by both table and target registry
		defs.addFromProperties(this.finder, c, table);
		
		return props;
	}
	/** Add a property definition
	 * 
	 */
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous){
		previous.getAllFrom(defs);
	    return previous;
	}
	/** Transition to define a new property
	 * 
	 * @author spb
	 *
	 */
	public class AddPropertyTransition extends AbstractFormTransition<DataObjectFactory>{

		public final class AddPropertyAction extends FormAction {
			private final DataObjectFactory target;
			public AddPropertyAction(DataObjectFactory target){
				this.target=target;
			}
			@SuppressWarnings("unchecked")
			@Override
			public FormResult action(Form f)
					throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
				((ConfigPropertyRegistry)f.getItem("Finder")).addDefinition(getContext(), (String)f.get("Name"), (String)f.get("Type"));
				return new ViewTableResult(target);
			}
		}

		public void buildForm(Form f, DataObjectFactory target,
				AppContext conn) throws TransitionException {
			SetInput<ConfigPropertyRegistry> input=new SetInput<>();
			for(PropertyFinder pf : props.getNested()){
				input.addChoice((ConfigPropertyRegistry)pf);
			}
			f.addInput("Finder", "Registy", input);
			f.addInput("Name", "Name of new property", new PatternTextInput(PropertyTag.PROPERT_TAG_NAME_PATTERN));
			SetInput<String> type = new SetInput<>();
			type.addChoice("Number");
			type.addChoice("String");
			type.addChoice("Date");
			for(String tag : getContext().getClassMap(DataObjectFactory.class).keySet()){
				type.addChoice(tag);
			}
			f.addInput("Type","Type of property",type);
			f.addAction("Add", new AddPropertyAction(target));
		}

	}
	public class AddDerivedTransition extends AbstractFormTransition<DataObjectFactory>{

		public final class AddDerivedAction extends FormAction {
			private final DataObjectFactory target;
			public AddDerivedAction(DataObjectFactory target){
				this.target=target;
			}
			@Override
			public FormResult action(Form f)
					throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
				try {
					//PropertyTagInput input = (PropertyTagInput) f.getInput("Prop");
					defs.addConfigProperty(getContext(), finder, table, ((PropertyTag)f.getItem("Prop")), (String)f.get("Expr"));
				} catch (Exception e) {
					getLogger().error( "Error setting derived prop",e);
					throw new ActionException("Operation failed",e);
				}
				return new ViewTableResult(target);
			}
		}

		public void buildForm(Form f, DataObjectFactory target,
				AppContext ctx) throws TransitionException {
			f.addInput("Prop", "Property to define", new PropertyTagInput(finder));
			f.addInput("Expr", "Definition", new PropExpressionInput(getContext(),finder));
			f.addAction("Add", new AddDerivedAction(target));
		}
	}
	public Map<TableTransitionKey, Transition> getTableTransitions() {
		Map<TableTransitionKey,Transition> result = new HashMap<>();
		// add transitions here
		result.put(new AdminOperationKey( "AddDefinition","Add a new property definition"),new AddDerivedTransition());
		result.put(new AdminOperationKey( "AddProperty","Define a new property"),new AddPropertyTransition());
		return result;
	}
	
	@Override
	public void getTableTransitionSummary(ContentBuilder hb, SessionService operator) {
		hb.addText("This policy allows derived property expressions to be defined for properties in scope. You can also define entirely new properties");
		
	}

}