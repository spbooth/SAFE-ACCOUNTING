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
package uk.ac.ed.epcc.safe.accounting.db.transitions;



import uk.ac.ed.epcc.safe.accounting.aggregation.AggregateUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.factory.FormCreator;
import uk.ac.ed.epcc.webapp.forms.inputs.ClassInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ConstructedObjectInput;
import uk.ac.ed.epcc.webapp.forms.inputs.OptionalListInputWrapper;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.jdbc.table.TableListResult;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.NewTableInput;

/** Class to create Aggregation tables
 * 
 * @author spb
 *
 */


public class AggregationTableCreator extends AbstractContexed implements FormCreator{

	
	private static final String PARENT_TAG = "parent_tag";
	private static final String HANDLER = "Handler";
	private static final String TABLE = "Table";
	
	public AggregationTableCreator(AppContext c){
		super(c);
	}
	public void buildCreationForm(Form f) throws Exception {
		f.addInput(TABLE, "Name of table to create", new NewTableInput(conn));
		ClassInput<AggregateUsageRecordFactory> handler_input = new ClassInput<>(conn, AggregateUsageRecordFactory.class);
		handler_input.setValue(conn.getInitParameter("aggregate_handler.default", "DailyUsageRecordFactory"));
		f.addInput(HANDLER,"Table handler type",handler_input);
		ConstructedObjectInput<UsageRecordFactory> parent_input = new ConstructedObjectInput<>(conn, UsageRecordFactory.class);
		f.addInput(PARENT_TAG, "Factory to aggregate", new OptionalListInputWrapper<>(parent_input));
		
	
		f.addAction("Create", new CreateAction());
	}

	public class CreateAction extends FormAction{

		@Override
		public FormResult action(Form f) throws ActionException {
			try{
				String table_name=(String) f.get(TABLE);
				String handler_tag = (String) f.get(HANDLER);
				String parent_tag = (String) f.get(PARENT_TAG);
				
				ConfigService serv = conn.getService(ConfigService.class);
				
				serv.setProperty("class."+table_name, handler_tag);
				if( parent_tag != null ) {
					serv.setProperty(AggregateUsageRecordFactory.PARENT_PREFIX+table_name, parent_tag);
				}
				
				return new TableListResult();
			}catch(Exception e){
				conn.getService(LoggerService.class).getLogger(getClass()).error("Error creating table",e);
				throw new ActionException("Create failed");
			}
		}
		
	}

}