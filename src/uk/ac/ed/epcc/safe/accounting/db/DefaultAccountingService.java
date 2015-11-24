// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.UsageManager;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.AppContextService;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** Service used to configure the Accounting
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: DefaultAccountingService.java,v 1.5 2014/09/15 14:32:20 spb Exp $")

public class DefaultAccountingService implements Contexed, AccountingService{
	private AppContext c;
	public DefaultAccountingService(AppContext c){
		this.c=c;
	}
	public UsageManager getUsageManager(){
		return getUsageManager("accounting");
	}
	public UsageManager getUsageManager(String name){
		return new ConfigUsageManager(c,name);
	}
	public UsageProducer getUsageProducer(){
		return getUsageManager();
	}
	public UsageProducer getUsageProducer(String name){
		
		if(name.contains(":")){
			UsageManager m = getUsageManager(name.substring(0, name.indexOf(':')));
			return m.parseProducer(name.substring(name.indexOf(':')+1));
		}else{
			// try a direct implementation first
			UsageProducer up = getContext().makeObjectWithDefault(UsageProducer.class, null, name);
			if( up != null ){
				return up;
			}
			UsageManager man = getUsageManager(name);
			if( man.hasProducers()){
				return man;
			}
			return null;
		}
	}
	/** Does the User have access to all the specified records
	 * 
	 * @param user AppUser making request
	 * @param sel RecordSelector requested
	 * @return boolean
	 */
	public boolean allow(SessionService user, RecordSelector sel){
		return true;
	}
	public AppContext getContext() {
		return c;
	}
	public void cleanup() {
		
	}
	public DefaultAccountingService copy(AppContext p)
			throws CloneNotSupportedException {
		DefaultAccountingService serv = (DefaultAccountingService) clone();
		serv.c=p;
		return serv;
	}
	public Class<? super AccountingService> getType() {
		return AccountingService.class;
	}
}