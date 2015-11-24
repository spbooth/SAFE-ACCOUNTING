// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db.transitions;



import uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactory;
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
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.jdbc.table.TableListResult;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.NewTableInput;

/** Class to create ConfigUsageRecordFactory accounting tables
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: AccountingTableCreator.java,v 1.14 2014/12/01 16:28:25 spb Exp $")

public class AccountingTableCreator implements FormCreator,Contexed{

	private static final String PARSER = "Parser";
	private static final String TABLE = "Table";
	private AppContext conn;
	public AccountingTableCreator(AppContext c){
		this.conn=c;
	}
	public void buildCreationForm(String type_name,Form f) throws Exception {
		f.addInput(TABLE, "Name of table to create", new NewTableInput(conn));
		f.addInput(PARSER,"Parser type",new ClassInput<PropertyContainerParser>(conn, PropertyContainerParser.class));
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
				String parser_tag = (String) f.get(PARSER);
				ClassInput<PropertyContainerParser> input = (ClassInput<PropertyContainerParser>) f.getInput(PARSER);
				ConfigService serv = conn.getService(ConfigService.class);
				serv.setProperty("class."+ConfigPlugInOwner.PARSER_PREFIX+table_name, parser_tag);
				serv.setProperty("class."+table_name, "ConfigUsageRecordFactory");
				
				// Note that the following step is only needed if auto_table is not enabled.
				PropertyContainerParser parser = conn.makeObject(input.getItem());
				TableSpecification spec = parser.modifyDefaultTableSpecification(conn,new TableSpecification(),null,table_name);
				if( spec != null ){
					ParseUsageRecordFactory.bootstrapTable(conn, table_name, spec);
				}
				return new TableListResult();
			}catch(Exception e){
				conn.error(e,"Error creating table");
				throw new ActionException("Create failed");
			}
		}
		
	}

}