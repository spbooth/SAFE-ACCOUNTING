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
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.UsageManager;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.ConfigurationException;

/** A {@link UsageRecordUsageManager} that is configured from the config service.
 * 
 * @author spb
 *
 */



public class ConfigUsageManager extends UsageManager {
	private static final String CONFIG_SUFFIX = ".tables";


	private ConfigUsageManager(AppContext c,String mytag) {
		super(c,mytag);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	protected void populate(String tag) {
		AppContext c = getContext();
		String tables=c.getExpandedProperty(tag+CONFIG_SUFFIX,tag);
		Logger log = c.getService(LoggerService.class).getLogger(getClass());
		AccountingService serv = c.getService(AccountingService.class);
		if( tables != null && ! tables.isEmpty()){
			for(String tab : tables.split(",")){
				tab=tab.trim();
				log.debug("consider "+tab);
				try{
					// don't supply a default we don't want tables created when
					// illegal producers requested.
					UsageProducer producer = serv.getUsageProducer(tab);
					if( producer != null ){
						String desc=c.getInitParameter("description."+tab, tab);
						if( producer instanceof DataObjectFactory){
							if( !((DataObjectFactory)producer).isValid() ){
								throw new ConfigurationException("Table "+tab+" Not valid table");
							}
						}
						addProducer(desc, producer);
					}else{
						if( c.getInitParameter(AppContext.CLASS_PREFIX+tab) == null) {
							getLogger().error("Property "+AppContext.CLASS_PREFIX+tab+" not set");
						}
						getLogger().error("No valid producer for "+tab);
					}
				}catch(Exception e){
					getLogger().error("Error making UsageProducer "+tag,e);
				}
			}
		
		}else{
			// Only warn as often triggered by safe default producer
			getLogger().warn("No tables specified for ConfigUsageManager tag "+tag+CONFIG_SUFFIX);
		}

	}


	public static ConfigUsageManager getInstance(AppContext conn,String tag) {
		String prop = conn.getInitParameter(tag+CONFIG_SUFFIX);
		if( prop == null || prop.isEmpty()) {
			return null;
		}
		return new ConfigUsageManager(conn, tag);
	}
	


	

}