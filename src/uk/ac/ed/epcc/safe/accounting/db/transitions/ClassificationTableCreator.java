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



//import uk.ac.ed.epcc.safe.accounting.db.AccountingClassificationFactory;
import uk.ac.ed.epcc.safe.accounting.db.PropertyTargetClassificationFactory;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
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
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.NewTableInput;

/** Class to create ConfigUsageRecordFactory accounting tables
 * 
 * @author spb
 *
 */


public class ClassificationTableCreator implements FormCreator,Contexed{


	private static final String TYPE = "Type";
	private static final String TABLE = "Table";
	private AppContext conn;
	public ClassificationTableCreator(AppContext c){
		this.conn=c;
	}
	public void buildCreationForm(String type_name,Form f) throws Exception {
		f.addInput(TABLE, "Name of table to create", new NewTableInput(conn));
		f.addInput(TYPE,"Table type",new ClassInput<PropertyTargetClassificationFactory>(conn, PropertyTargetClassificationFactory.class));
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
				String type_tag = (String) f.get(TYPE);
				ClassInput<PropertyTargetClassificationFactory> input = (ClassInput<PropertyTargetClassificationFactory>) f.getInput(TYPE);
				ConfigService serv = conn.getService(ConfigService.class);
				serv.setProperty("class."+table_name, type_tag);
			    Class<? extends PropertyTargetClassificationFactory> target = input.getItem();
			    if( DataObjectFactory.AUTO_CREATE_TABLES_FEATURE.isEnabled(conn)){
			    	conn.makeObject(target, table_name);
			    }else{
			    	// best we can do
			    	TableSpecification spec = Classification.getTableSpecification(conn);
					DataBaseHandlerService handler = conn.getService(DataBaseHandlerService.class);
					if( handler != null ){
						handler.createTable(table_name, spec);
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