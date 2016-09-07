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
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionTarget;
import uk.ac.ed.epcc.webapp.jdbc.table.TransitionSource;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.transition.TransitionKey;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** This policy allows derived property expressions to be defined for 
 * properties in scope. It also provides a mechanism for {@link ConfigPropertyRegistry} objects to
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
 * 
 * @author spb
 *
 */


public class ExpressionPropertyPolicy extends BasePolicy implements TransitionSource<TableTransitionTarget>, SummaryProvider{
	private PropExpressionMap defs=new PropExpressionMap();	
	private AppContext c;
	private String table;
	private MultiFinder finder; // finder to use for expression parse
	private MultiFinder props; // defined props
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		this.c=ctx;
		this.table=table;
		this.finder=new MultiFinder();
		this.finder.addFinder(prev);
		
		props = new MultiFinder();
		String list = ctx.getInitParameter("registry_list."+table, "expression");
		for( String name : list.split("\\s*,\\s*")){
			props.addFinder(new ConfigPropertyRegistry(ctx, name));
		}
		this.finder.addFinder(props);
		
		defs.addFromProperties(this.finder, ctx, table);
		
		return props;
	}
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous){
		previous.getAllFrom(defs);
	    return previous;
	}
	public class AddPropertyTransition extends AbstractFormTransition<TableTransitionTarget>{

		public final class AddPropertyAction extends FormAction {
			private final TableTransitionTarget target;
			public AddPropertyAction(TableTransitionTarget target){
				this.target=target;
			}
			@SuppressWarnings("unchecked")
			@Override
			public FormResult action(Form f)
					throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
				SetInput<ConfigPropertyRegistry> input=(SetInput<ConfigPropertyRegistry>) f.getInput("Finder");	
				input.getItem().addDefinition(c, (String)f.get("Name"), (String)f.get("Type"));
				return new ViewTableResult(target);
			}
		}

		public void buildForm(Form f, TableTransitionTarget target,
				AppContext conn) throws TransitionException {
			SetInput<ConfigPropertyRegistry> input=new SetInput<ConfigPropertyRegistry>();
			for(PropertyFinder pf : props.getNested()){
				input.addChoice((ConfigPropertyRegistry)pf);
			}
			f.addInput("Finder", "Registy", input);
			f.addInput("Name", "Name of new property", new PatternTextInput(PropertyTag.PROPERT_TAG_NAME_PATTERN));
			SetInput<String> type = new SetInput<String>();
			type.addChoice("Number");
			type.addChoice("String");
			type.addChoice("Date");
			for(String tag : c.getClassMap(DataObjectFactory.class).keySet()){
				type.addChoice(tag);
			}
			f.addInput("Type","Type of property",type);
			f.addAction("Add", new AddPropertyAction(target));
		}

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
					PropertyTagInput input = (PropertyTagInput) f.getInput("Prop");
					defs.addConfigProperty(c, finder, table, input.getItem(), (String)f.get("Expr"));
				} catch (Throwable e) {
					getLogger().error( "Error setting derived prop",e);
					throw new ActionException("Operation failed",e);
				}
				return new ViewTableResult(target);
			}
		}

		public void buildForm(Form f, TableTransitionTarget target,
				AppContext ctx) throws TransitionException {
			f.addInput("Prop", "Property to define", new PropertyTagInput(finder));
			f.addInput("Expr", "Definition", new PropExpressionInput(c,finder));
			f.addAction("Add", new AddDerivedAction(target));
		}
	}
	public Map<TransitionKey<TableTransitionTarget>, Transition<TableTransitionTarget>> getTransitions() {
		Map<TransitionKey<TableTransitionTarget>,Transition<TableTransitionTarget>> result = new HashMap<TransitionKey<TableTransitionTarget>, Transition<TableTransitionTarget>>();
		// add transitions here
		result.put(new TransitionKey<TableTransitionTarget>(TableTransitionTarget.class, "AddDefinition"),new AddDerivedTransition());
		result.put(new TransitionKey<TableTransitionTarget>(TableTransitionTarget.class, "AddProperty"),new AddPropertyTransition());
		return result;
	}
	protected final Logger getLogger(){
		return c.getService(LoggerService.class).getLogger(getClass());
	}
	@Override
	public void getTableTransitionSummary(ContentBuilder hb, SessionService operator) {
		hb.addText("This policy allows derived property expressions to be defined for properties in scope. You can also define entirely new properties");
		
	}

}