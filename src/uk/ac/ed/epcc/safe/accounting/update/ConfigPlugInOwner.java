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
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.FormValidator;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.ValidateException;
import uk.ac.ed.epcc.webapp.forms.inputs.ClassInput;
import uk.ac.ed.epcc.webapp.forms.inputs.SetInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;

/** A {@link PlugInOwner} configured from the config-service.
 * 
 * Set <em><b>parser.</b>tag</em> to configure the parser class.
 * Set <em><b>policies.</b>tag</em> to a list of policies to apply.
 * @author spb
 *
 * @param <T> type of target
 */


public class ConfigPlugInOwner<T extends DataObjectFactory,R> extends AbstractPlugInOwner<T,R> {
	public static final String POLICIES_PREFIX = "policies.";
	public static final String PARSER_PREFIX = "parser.";
	final Class<? extends PropertyContainerParser> default_parser_class;
	public ConfigPlugInOwner(AppContext c, PropertyFinder prev,String tag,Class<? extends PropertyContainerParser> default_parser_class) {
		super(c,prev, tag);
		this.default_parser_class=default_parser_class;
	}
	public ConfigPlugInOwner(AppContext c, PropertyFinder prev,String tag) {
		super(c,prev, tag);
		this.default_parser_class=ReadOnlyParser.class;
	}
	@SuppressWarnings("unchecked")
	@Override
	protected PropertyContainerParser<R> makeParser() {
		AppContext ctx = getContext();
			PropertyContainerParser<R> parser=null;
		    	Class<? extends PropertyContainerParser> parser_class = ctx.getPropertyClass(PropertyContainerParser.class,default_parser_class, PARSER_PREFIX+getTag());
		    	if( parser_class != null ){
		    		try {
						parser = ctx.makeObject(parser_class);
					} catch (Exception e) {
						getLogger().error("Error making parser",e);
					}
		    	}
			return parser;
		
	}

	
		@Override
		protected Set<PropertyContainerPolicy> makePolicies() {
			AppContext ctx = getContext();
			Logger log = ctx.getService(LoggerService.class).getLogger(getClass());
			Set<PropertyContainerPolicy> policies= new LinkedHashSet<PropertyContainerPolicy>();
	    	String policy_list = ctx.getInitParameter(POLICIES_PREFIX+getTag());
	    	log.debug("policy list="+policy_list);
	    	if( policy_list != null){


	    		for(String pol : policy_list.trim().split(",")){
	    			log.debug("Try making "+pol);
	    			try{
	    				Class<? extends PropertyContainerPolicy> pol_class = ctx.getClassFromName(PropertyContainerPolicy.class, null, pol);
	    				if( pol_class != null){
	    					policies.add(ctx.makeObject(pol_class));
	    					log.debug("made ok");
	    				}else{
	    					getLogger().error("Bad Policy class "+pol);
	    				}
	    			}catch(Throwable e){
	    				getLogger().error("Error making policy ",e);
	    			}
	    		}
	    	}
	    	log.debug("policies size is "+policies.size());
			return policies;
		}
		
		protected SetInput getPolicyInput(){
			 AppContext ctx = getContext();
			SetInput<Class> input = new SetInput<Class>();
			String policy_list = ctx.getInitParameter(POLICIES_PREFIX+getTag());
	       
	    	if( policy_list != null){
	    		
	    	
	    	for(String pol : policy_list.trim().split(",")){
	    		try{
	    		Class<? extends PropertyContainerPolicy> pol_class = ctx.getClassFromName(PropertyContainerPolicy.class, null, pol);
	    		if( pol_class != null){
	    			input.addChoice(pol, pol_class);
	    		}else{
	    			getLogger().error("Bad Policy class "+pol);
	    		}
	    		}catch(Exception e){
	    			getLogger().error("Error making policy ",e);
	    		}
	    	}
	    	}
			return input;
		}
		public  final class DeletePolicyAction extends FormAction {
			private final AppContext conn;
			private final T target;
			public DeletePolicyAction(AppContext conn,T target){
				this.conn=conn;
				this.target=target;
			}
			@SuppressWarnings("unchecked")
			@Override
			public FormResult action(Form f)
					throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
				SetInput<Class> policy_input = (SetInput<Class>) f.getInput("Policy");
				StringBuilder sb = new StringBuilder();
				boolean seen=false;
				for(Iterator<Class> it = policy_input.getItems(); it.hasNext();){
					Class c = it.next();
					if( ! c.equals(policy_input.getItem())){
						if( seen ){
							sb.append(",");
						}else{
							seen=true;
						}
						sb.append(policy_input.getText(c));
					}
				}
				ConfigService serv = conn.getService(ConfigService.class);
				serv.setProperty(POLICIES_PREFIX+target.getTag(), sb.toString());
				return new ViewTableResult(target);
			}
		}
		public class DeletePolicyTransition extends AbstractFormTransition<T>{

			

			public void buildForm(Form f, T target,
					AppContext c) throws TransitionException {
				f.addInput("Policy","Policy to remove", getPolicyInput());
				f.addAction("Delete", new DeletePolicyAction(getContext(),target));
				
			}
		}
		public  final class AddPolicyAction extends FormAction {
			private final T target;
			public AddPolicyAction(T target){
				this.target=target;
			}
			@SuppressWarnings("unchecked")
			@Override
			public FormResult action(Form f)
					throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
				ClassInput<PropertyContainerPolicy> input = (ClassInput<PropertyContainerPolicy>) f.getInput("Policy");
				ConfigService serv = getContext().getService(ConfigService.class);
				String list = serv.getServiceProperties().getProperty(POLICIES_PREFIX+target.getTag());
				if( list == null || list.trim().length() == 0){
					list = input.getValue();
				}else{
					list = list+","+input.getValue();
				}
				serv.setProperty(POLICIES_PREFIX+target.getTag(), list);
				return new ViewTableResult(target);
			}
		}
		public class AddPolicyTransition extends AbstractFormTransition<T>{

			

			public void buildForm(Form f, T target,
				AppContext c) throws TransitionException {
				
				ClassInput<PropertyContainerPolicy> input = new ClassInput<PropertyContainerPolicy>(getContext(), PropertyContainerPolicy.class);
				
				f.addInput("Policy", "Policy to Add", input);
				f.addAction("Add", new AddPolicyAction(target));
					f.addValidator(new FormValidator() {
					
					@SuppressWarnings("unchecked")
					public void validate(Form f) throws ValidateException {
						ClassInput<PropertyContainerPolicy> input = (ClassInput<PropertyContainerPolicy>) f.getInput("Policy");
						Class target = input.getItem();
						for( PropertyContainerPolicy pol : getPolicies()){
							if( pol.getClass() == target){
								throw new ValidateException("This Policy is already in force");
							}
						}
					}
				});
			}
		}
		public  final class SetParserAction extends FormAction {
			private final T target;
			public SetParserAction(T target){
				this.target=target;
			}
			@SuppressWarnings("unchecked")
			@Override
			public FormResult action(Form f)
					throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
				ClassInput<PropertyContainerParser> input = (ClassInput<PropertyContainerParser>) f.getInput("parser");
				ConfigService serv = getContext().getService(ConfigService.class);
				
				serv.setProperty(PARSER_PREFIX+target.getTag(), input.getValue());
				return new ViewTableResult(target);
			}
		}
		public class SetParserTransition extends AbstractFormTransition<T>{

			public void buildForm(Form f, T target, AppContext conn)
					throws TransitionException {
				ClassInput<PropertyContainerParser> input = new ClassInput<PropertyContainerParser>(conn, PropertyContainerParser.class);
				f.addInput("parser", "Parser class", input);
				f.addAction("Set",new SetParserAction(target) );
			}
			
		}
		
		@Override
		public Map<TableTransitionKey, Transition<? extends DataObjectFactory>> getTableTransitions() {
			
			Map<TableTransitionKey, Transition<? extends DataObjectFactory>> res = super.getTableTransitions();
			res.put(new AdminOperationKey("DeletePolicy","Remove one of the configured policies"), new DeletePolicyTransition());
			res.put(new AdminOperationKey("AddPolicy","Add a new policy"), new AddPolicyTransition());
			if( getParser().getClass() == default_parser_class){
				res.put(new AdminOperationKey("SetParser","Set a table parser"), new SetParserTransition());
			}

			return res;
		}
		/* (non-Javadoc)
		 * @see uk.ac.ed.epcc.safe.accounting.update.AbstractPlugInOwner#addConfigParameters(java.util.Set)
		 */
		@Override
		public void addConfigParameters(Set<String> params) {
			params.add(AppContext.CLASS_PREFIX+PARSER_PREFIX+getTag());
			params.add(POLICIES_PREFIX+getTag());
			super.addConfigParameters(params);
		}
}