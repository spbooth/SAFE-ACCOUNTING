// Copyright - The University of Edinburgh 2011
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
@uk.ac.ed.epcc.webapp.Version("$Id: ConfigUsageManager.java,v 1.27 2014/09/15 14:32:20 spb Exp $")


public class ConfigUsageManager extends UsageRecordUsageManager {
	public ConfigUsageManager(AppContext c,String mytag) {
		super(c,mytag);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	protected void populate(String tag) {
		AppContext c = getContext();
		String tables=c.getInitParameter(tag+".tables",tag);
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
						c.error("No valid producer for "+tab);
					}
				}catch(Throwable e){
					c.error(e,"Error making UsageProducer "+tag);
				}
			}
		}else{
			c.error("No tables specified for ConfigUsageManager tag "+tag+".tables");
		}

	}


	public boolean canUpdate(SessionService c) {
		return true;
	}


	


	

}