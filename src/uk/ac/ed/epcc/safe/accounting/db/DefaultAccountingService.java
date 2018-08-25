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
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.UsageManager;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.UsageProducerWrapper;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;

/** Service used to configure the Accounting
 * @author spb
 *
 */


public class DefaultAccountingService extends AbstractContexed implements AccountingService{
	
	public static final String DEFAULT_PRODUCER_NAME = "accounting";
	public DefaultAccountingService(AppContext c){
		super(c);
	}
	public UsageManager getUsageManager(){
		return getUsageManager(DEFAULT_PRODUCER_NAME);
	}
	public UsageManager getUsageManager(String name){
		return ConfigUsageManager.getInstance(getContext(),name);
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
			ExpressionTargetFactory etf = ExpressionCast.makeExpressionTargetFactory(getContext(), name);
			if( etf != null) {
				if( etf instanceof UsageProducer) {
					return (UsageProducer) etf;
				}else {
					return new UsageProducerWrapper(getContext(),name,etf);
				}
			}
			UsageManager man = getUsageManager(name);
			if( man != null && man.hasProducers()){
				return man;
			}
			return null;
		}
	}
	
	
	public void cleanup() {
		
	}
	
	public Class<? super AccountingService> getType() {
		return AccountingService.class;
	}
}