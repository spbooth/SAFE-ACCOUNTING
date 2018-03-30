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
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.AppContextService;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** Service used to configure the Accounting
 * @author spb
 *
 */


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