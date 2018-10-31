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
package uk.ac.ed.epcc.safe.accounting.allocations;



import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.factory.FormCreator;
import uk.ac.ed.epcc.webapp.forms.inputs.ClassInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.transition.ExtraContent;
import uk.ac.ed.epcc.webapp.jdbc.table.TableListResult;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.NewTableInput;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** Class to create {@link AllocationManager} accounting tables
 * 
 * @author spb
 *
 */


public class AllocationTableCreator extends AbstractContexed implements FormCreator, ExtraContent{


	private static final String TYPE = "Type";
	private static final String TABLE = "Table";

	public AllocationTableCreator(AppContext c){
		super(c);
	}
	public void buildCreationForm(String type_name,Form f) throws Exception {
		f.addInput(TABLE, "Name of table to create", new NewTableInput(conn));
		f.addInput(TYPE,"Table type",new ClassInput<>(conn, AllocationManager.class));
		f.addAction("Create", new CreateAction());
	}

	public class CreateAction extends FormAction{

		
		@Override
		public FormResult action(Form f) throws ActionException {
			try{
				String table_name=(String) f.get(TABLE);
				String type_tag = (String) f.get(TYPE);
				
				ConfigService serv = conn.getService(ConfigService.class);
				serv.setProperty("class."+table_name, type_tag);
			    // relies on auto_create_tables
				return new TableListResult();
			}catch(Exception e){
				conn.getService(LoggerService.class).getLogger(getClass()).error("Error creating table",e);
				throw new ActionException("Create failed");
			}
		}
		
	}
	@Override
	public ContentBuilder getExtraHtml(ContentBuilder cb, SessionService op, Object target) {
	    cb.addText("This form creates a new allocation table. Allocation tables are populated by forms and hold some resource allocation. "+
	    		"Policies then decrement allocation as accounting data is loaded.");
		return cb;
	}

}