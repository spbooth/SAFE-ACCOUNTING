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



import sun.reflect.generics.repository.ConstructorRepository;
import uk.ac.ed.epcc.safe.accounting.aggregation.AggregateUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.update.ConfigPlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
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
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.NewTableInput;

/** Class to create Aggregation tables
 * 
 * @author spb
 *
 */


public class AggregationTableCreator implements FormCreator,Contexed{

	
	private static final String MASTER_TAG = "master_tag";
	private static final String HANDLER = "Handler";
	private static final String TABLE = "Table";
	private AppContext conn;
	public AggregationTableCreator(AppContext c){
		this.conn=c;
	}
	public void buildCreationForm(String type_name,Form f) throws Exception {
		f.addInput(TABLE, "Name of table to create", new NewTableInput(conn));
		ClassInput<AggregateUsageRecordFactory> handler_input = new ClassInput<AggregateUsageRecordFactory>(conn, AggregateUsageRecordFactory.class);
		handler_input.setValue(conn.getInitParameter("aggregate_handler.default", "DailyUsageRecordFactory"));
		f.addInput(HANDLER,"Table handler type",handler_input);
		ConstructedObjectInput<UsageRecordFactory> master_input = new ConstructedObjectInput<>(conn, UsageRecordFactory.class);
		f.addInput(MASTER_TAG, "Factory to aggregate", new OptionalListInputWrapper<>(master_input));
		
	
		f.addAction("Create", new CreateAction());
	}

	public AppContext getContext() {
		return conn;
	}
	public class CreateAction extends FormAction{

		@SuppressWarnings("unchecked")
		@Override
		public FormResult action(Form f) throws ActionException {
			try{
				String table_name=(String) f.get(TABLE);
				String handler_tag = (String) f.get(HANDLER);
				String master_tag = (String) f.get(MASTER_TAG);
				
				ConfigService serv = conn.getService(ConfigService.class);
				
				serv.setProperty("class."+table_name, handler_tag);
				if( master_tag != null ) {
					serv.setProperty(AggregateUsageRecordFactory.MASTER_PREFIX+table_name, master_tag);
				}
				
				return new TableListResult();
			}catch(Exception e){
				conn.getService(LoggerService.class).getLogger(getClass()).error("Error creating table",e);
				throw new ActionException("Create failed");
			}
		}
		
	}

}