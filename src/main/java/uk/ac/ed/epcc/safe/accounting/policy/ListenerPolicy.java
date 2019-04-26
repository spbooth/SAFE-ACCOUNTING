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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.UsageRecordListener;
import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.ExtendedXMLBuilder;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.SetInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.table.AdminOperationKey;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.jdbc.table.ViewTableResult;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.TableInput;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** The AccountingListenerPolicy forwards record create and delete operations
 * onto a set of {@link UsageRecordListener} classes. For example to build aggregate tables or
 * to decrement budgets.
 * 
 * This policy is unusual in that it does not generate any new properties only consume them.
 * 
 * Configuration Properties:
 * <ul>
 * <li> <b>UsageRecordListener.<i>table-name</i></b> defines a comma separated list of 
 * remote tables we are updating.</li>
 * 
 * </ul>
 * 
 * 
 * @author spb
 *
 */



public class ListenerPolicy extends BaseUsageRecordPolicy implements SummaryProvider,TableTransitionContributor,ConfigParamProvider{
    public ListenerPolicy(AppContext conn) {
		super(conn);
	}
	private static final String PREFIX = "UsageRecordListener.";

    private String list;
    private String table;
    private int updates;

	@Override
	public void postCreate(PropertyContainer props, ExpressionTargetContainer rec)
			throws Exception {
		if( listeners != null ){
			for(UsageRecordListener fac : listeners){
				if( fac != null ){
					fac.postCreate(props, rec);
				}
			}
		}else{
			getLogger().warn("null listeners");
		}
		updates++;
	}
	@Override
	public void preDelete(ExpressionTargetContainer rec) throws Exception {
		if( listeners != null){
			for(UsageRecordListener fac : listeners){
				if( fac != null ){
					fac.preDelete(rec);
				}
			}
		}else{
			getLogger().warn("null listeners");
		}

	}
	private UsageRecordListener listeners[];
	
	@Override
	public PropertyFinder initFinder(PropertyFinder prev,
			String table) {
		
		this.table=table;
		list =getContext().getInitParameter(PREFIX+table, "");
		getLogger().debug("ListenerPolicy: table="+table+" list="+list);
		return null;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.BasePolicy#startParse(uk.ac.ed.epcc.safe.accounting.PropertyContainer)
	 */
	@Override
	public void startParse(PropertyContainer defaults) throws DataException,
	InvalidPropertyException {
		updates=0;
		// defer the creation of the AggregateUsageRecordFactory classes to the parse stage.
		// remember the master object created internally contains this policy
		// so if we don't delay creation we get infinite recursion.
		listeners=makeListeners().toArray(new UsageRecordListener[0]);
		

	}
	/** Create the list of listeners
	 * 
	 */
	protected LinkedList<UsageRecordListener> makeListeners() {
		Logger log = getLogger();
		LinkedList<UsageRecordListener> result= new LinkedList<>();
		log.debug("ListenerPolicy list="+list);
		if( list != null && list.trim().length() > 0){
			String tables[] = list.trim().split(",");
		
			for(int i=0;i<tables.length;i++){
				String aggregate_table=tables[i].trim();
				if( aggregate_table.length() > 0 ){
					log.debug("create "+aggregate_table);
					try {	
						UsageRecordListener tmp = getContext().makeObjectWithDefault(UsageRecordListener.class, null,aggregate_table);
						tmp.startListenerParse();
						result.add(tmp);
					} catch (Exception e) {
						getLogger().error("Error making UsageRecordListner list="+list+" table="+tables[i],e);
					}
				}
			}
		}
		return result;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.BasePolicy#endParse()
	 */
	@Override
	public String endParse() {
		StringBuilder sb = new StringBuilder();
		boolean seen=false;
		if( listeners != null){
		for( int i=0 ; i <  listeners.length ; i++){
			UsageRecordListener fac = listeners[i];
			if( fac != null ){
				seen=true;
				sb.append("listner[");
				sb.append(i);
				sb.append("] ");
				sb.append(fac.endListenerParse());
				sb.append("\n");
				listeners[i]=null;
			}
		}
		}
		sb.append("\nupdates=");
		sb.append(updates);
		sb.append("\n");
		if( ! seen ){
			sb.append("No listeners configured");
		}
		listeners=null;
		updates=0;
		getLogger().info(sb.toString());
		return "";
	}
	public void getTableTransitionSummary(ContentBuilder hb,
			SessionService operator) {
		
		hb.addText("This policy forwards record creat/delete events to a listener, e.g. an aggregation table or allocation manager");
	
		
		if( list != null && list.trim().length()>0){
			ExtendedXMLBuilder xml = hb.getText();
			xml.open("ul");
			for(String s : list.split(",")){
				xml.open("li");
				xml.clean(s);
				xml.close();
			}
			xml.close();
			xml.appendParent();
		}else{
			hb.addText("No listeners configured");
		}
	}
	public final class DeleteListenerAction extends FormAction {
		private final DataObjectFactory target;
		public DeleteListenerAction(DataObjectFactory target){
			this.target=target;
		}
		@SuppressWarnings("unchecked")
		@Override
		public FormResult action(Form f)
				throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
			SetInput<String> policy_input = (SetInput<String>) f.getInput("Listener");
			StringBuilder sb = new StringBuilder();
			boolean seen=false;
			for(Iterator<String> it = policy_input.getItems(); it.hasNext();){
				String c = it.next();
				if( ! c.equals(policy_input.getItem())){
					if( seen ){
						sb.append(",");
					}else{
						seen=true;
					}
					sb.append(policy_input.getText(c));
				}
			}
			ConfigService serv = getContext().getService(ConfigService.class);
			serv.setProperty(PREFIX+table, sb.toString());
			list=sb.toString();
			return new ViewTableResult(target);
		}
	}
	public class DeleteListenerTransition extends AbstractFormTransition<DataObjectFactory>{

		

		public void buildForm(Form f, DataObjectFactory target,
				AppContext c) throws TransitionException {
			SetInput<String> input = new SetInput<>();
			for(String s : list.split(",")){
				input.addChoice(s);
			}
			f.addInput("Listener","Listener to remove", input);
			f.addAction("Delete", new DeleteListenerAction(target));
			
		}
	}
	public class AddListenerTransition extends AbstractFormTransition<DataObjectFactory>{

		
		public final class AddListenerAction extends FormAction {
			private final DataObjectFactory target;
			public AddListenerAction(DataObjectFactory target){
				this.target=target;
			}
			@SuppressWarnings("unchecked")
			@Override
			public FormResult action(Form f)
					throws uk.ac.ed.epcc.webapp.forms.exceptions.ActionException {
				TableInput<UsageRecordListener> input = (TableInput<UsageRecordListener>) f.getInput("Listener");
				ConfigService serv = getContext().getService(ConfigService.class);
				String list = serv.getServiceProperties().getProperty(PREFIX+table);
				Set<String> set = new HashSet<>();
				for(String s : list.split(",")){
					set.add(s);
				}
				set.add(input.getValue());
				StringBuilder sb = new StringBuilder();
				for( String s : set ){
					if(sb.length() > 0){
						sb.append(",");
					}
					sb.append(s);
				}
				
				list=sb.toString();
				serv.setProperty(PREFIX+table, list);
				return new ViewTableResult(target);
			}
		}

		public void buildForm(Form f, DataObjectFactory target,
			AppContext c) throws TransitionException {
			
			TableInput<UsageRecordListener> input = new TableInput<>(getContext(), UsageRecordListener.class);
			
			f.addInput("Listener", "Listener to Add", input);
			f.addAction("Add", new AddListenerAction(target));
				
		}
	}
	public Map<TableTransitionKey, Transition> getTableTransitions() {
		Map<TableTransitionKey,Transition> result = new HashMap<>();
		result.put(new AdminOperationKey( "Add Listener"),new AddListenerTransition());
		if( list != null  && list.length() > 0){
			result.put(new AdminOperationKey("Remove Listener"),new DeleteListenerTransition());
		}
		return result;
	}
	
	@Override
	public void addConfigParameters(Set<String> params) {
		params.add(PREFIX+table);
		
	}

}