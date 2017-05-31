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

import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.ConfigurationException;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** A {@link UsageRecordUsageManager} that is configured from the config service.
 * 
 * @author spb
 *
 */



public class ConfigUsageManager extends UsageRecordUsageManager {
	public ConfigUsageManager(AppContext c,String mytag) {
		super(c,mytag);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	protected void populate(String tag) {
		AppContext c = getContext();
		String tables=c.getExpandedProperty(tag+".tables",tag);
		Logger log = c.getService(LoggerService.class).getLogger(getClass());
		if( tables != null ){
			for(String tab : tables.split(",")){
				tab=tab.trim();
				log.debug("consider "+tab);
				try{
					// don't supply a default we don't want tables created when
					// illegal producers requested.
					UsageProducer<UsageRecordFactory.Use> producer = c.makeObjectWithDefault(UsageProducer.class, null,tab);
					if( producer != null ){
						String desc=c.getInitParameter("description."+tab, tab);
						if( producer instanceof DataObjectFactory){
							if( !((DataObjectFactory)producer).isValid() ){
								throw new ConfigurationException("Table "+tab+" Not valid table");
							}
						}
						addProducer(desc, producer);
					}else{
						getLogger().error("No valid producer for "+tab);
					}
				}catch(Throwable e){
					getLogger().error("Error making UsageProducer "+tag,e);
				}
			}
		}else{
			getLogger().error("No tables specified for ConfigUsageManager tag "+tag+".tables");
		}

	}


	public boolean canUpdate(SessionService c) {
		return true;
	}


	


	

}