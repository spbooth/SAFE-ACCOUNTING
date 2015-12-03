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

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.UsageManager;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.factory.FormUpdate;
import uk.ac.ed.epcc.webapp.forms.factory.StandAloneFormUpdate;
import uk.ac.ed.epcc.webapp.forms.inputs.DateInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.OptionalInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataNotFoundException;
import uk.ac.ed.epcc.webapp.session.SessionService;

public abstract class UsageRecordUsageManager extends UsageManager<UsageRecordFactory.Use> {

	public UsageRecordUsageManager(AppContext c, String tag) {
		super(c, tag);
		// TODO Auto-generated constructor stub
	}

	 public class AccountingRecordUpdater implements StandAloneFormUpdate<UsageRecordFactory.Use>{
		  
	    	
	        private static final String TABLE_KEY="Table";
	        private static final String DATE_KEY="Date";
	        private static final String ID_KEY="Id";
	        
			
			public void buildSelectForm(Form f, String label, UsageRecordFactory.Use dat) {
				ListInput<String, UsageProducer> pt_input =getProducerInput(false);
				if( pt_input instanceof OptionalInput){
					((OptionalInput)pt_input).setOptional(false);
				}
				DateInput d_input = new DateInput();
				d_input.setOptional(false);
				TextInput t_input=new TextInput();
				t_input.setOptional(false);
				f.addInput(TABLE_KEY,"Table",pt_input);
				f.addInput(DATE_KEY,"Date",d_input);
				f.addInput(ID_KEY,"JobID",t_input);
				
				
			}

			@SuppressWarnings("unchecked")
			public void buildUpdateForm(String name, Form f, UsageRecordFactory.Use dat,SessionService operator) throws Exception {
				if( dat == null){
					return; // needed for tests
				}
				UsageRecordFactory fac = dat.getUsageRecordFactory();
				// delegate to the underlying factory.
				StandAloneFormUpdate u = (StandAloneFormUpdate) fac.getFormUpdate(getContext());
				u.buildUpdateForm(name,f, dat,operator);
				
			}

			@SuppressWarnings("unchecked")
			public UsageRecordFactory.Use getSelected(Form f) {
				DateInput d_input = (DateInput) f.getInput(DATE_KEY);
				TextInput t_input = (TextInput) f.getInput(ID_KEY);
				try{
					UsageRecordFactory<?> fac = (UsageRecordFactory) f.getItem(TABLE_KEY);
					AndRecordSelector sel = new AndRecordSelector();
					sel.add(new SelectClause(BatchParser.JOB_ID_PROP, t_input.getValue()));
					Date point = d_input.getValue();
					Date start = new Date(point.getTime() - 1000L * 24L * 3600L);
					Date end = new Date(point.getTime() + 1000L * 24L * 3600L);
					sel.add(new SelectClause(StandardProperties.ENDED_PROP,MatchCondition.GT,start));
					sel.add(new SelectClause(StandardProperties.ENDED_PROP,MatchCondition.LT,end));
					return  fac.find(sel);
				}catch(DataNotFoundException nfe){
					return null;
				}catch(Exception e){
					getContext().error(e,"Error in getSelected ");
					return null;
				}
				
			}

			public AppContext getContext() {
				return UsageRecordUsageManager.this.getContext();
			}

	    }
		public FormUpdate<UsageRecordFactory.Use> getFormUpdate(AppContext c) {
			return new AccountingRecordUpdater();
		}
}