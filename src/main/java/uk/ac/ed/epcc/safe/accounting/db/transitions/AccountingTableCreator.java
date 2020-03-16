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



import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.update.ConfigPlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.factory.FormCreator;
import uk.ac.ed.epcc.webapp.forms.inputs.ClassInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.jdbc.table.DataBaseHandlerService;
import uk.ac.ed.epcc.webapp.jdbc.table.TableListResult;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.NewTableInput;

/** Class to create ConfigUsageRecordFactory accounting tables
 * 
 * @author spb
 *
 */


public class AccountingTableCreator extends AbstractContexed implements FormCreator{

	private static final String PARSER = "Parser";
	private static final String HANDLER = "Handler";
	private static final String TABLE = "Table";
	public AccountingTableCreator(AppContext c){
		super(c);
	}
	public void buildCreationForm(String type_name,Form f) throws Exception {
		f.addInput(TABLE, "Name of table to create", new NewTableInput(conn));
		ClassInput<UsageRecordFactory> handler_input = new ClassInput<>(conn, UsageRecordFactory.class);
		handler_input.setValue(conn.getInitParameter("accounting_handler.default", "ConfigUsageRecordFactory"));
		f.addInput(HANDLER,"Table handler type",handler_input);
		f.addInput(PARSER,"Parser type",new ClassInput<>(conn, PropertyContainerParser.class));
	
		f.addAction("Create", new CreateAction());
	}

	public class CreateAction extends FormAction{

		@SuppressWarnings("unchecked")
		@Override
		public FormResult action(Form f) throws ActionException {
			try{
				String table_name=(String) f.get(TABLE);
				String handler_tag = (String) f.get(HANDLER);
				String parser_tag = (String) f.get(PARSER);
				//ClassInput<PropertyContainerParser> input = (ClassInput<PropertyContainerParser>) f.getInput(PARSER);
				ConfigService serv = conn.getService(ConfigService.class);
				serv.setProperty("class."+ConfigPlugInOwner.PARSER_PREFIX+table_name, parser_tag);
				serv.setProperty("class."+table_name, handler_tag);
				
				// Note that the following step is only needed if auto_table is not enabled.
				if( ! DataObjectFactory.AUTO_CREATE_TABLES_FEATURE.isEnabled(getContext())){
					// This assumes that the ConfigPlugInOwner does not add any additional
					// table specification e.g. by adding standard policies.
					// safer to auto-create if enabled.
					PropertyContainerParser parser = conn.makeObject((Class<? extends PropertyContainerParser>)f.getItem(PARSER));
					TableSpecification spec = parser.modifyDefaultTableSpecification(new TableSpecification(),null,table_name);
					if( spec != null ){
						DataBaseHandlerService dbh = conn.getService(DataBaseHandlerService.class);
						if( dbh != null ){
							dbh.createTable(table_name, spec);
						}
					}
				}
				return new TableListResult();
			}catch(Exception e){
				conn.getService(LoggerService.class).getLogger(getClass()).error("Error creating table",e);
				throw new ActionException("Create failed");
			}
		}
		
	}

}