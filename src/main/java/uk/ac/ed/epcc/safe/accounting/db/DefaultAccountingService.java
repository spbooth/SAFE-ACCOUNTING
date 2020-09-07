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

import java.util.HashMap;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.UsageManager;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.UsageProducerWrapper;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;

/** Service used to configure the Accounting
 * @author spb
 *
 */


public class DefaultAccountingService extends AbstractContexed implements AccountingService{
	public static final Feature DEFAULT_COMPOSITE_FEATURE = new Feature("accounting.producer.default_composite",false,"UsageProducers default to composite mode");
	public static final String DEFAULT_PRODUCER_NAME = "accounting";
	private Map<String,ConfigUsageManager> cache = new HashMap<>();
	public DefaultAccountingService(AppContext c){
		super(c);
	}
	public UsageManager getUsageManager(){
		return getUsageManager(DEFAULT_PRODUCER_NAME);
	}
	public UsageManager getUsageManager(String name){
		if( cache.containsKey(name)) {
			return cache.get(name);
		}
		ConfigUsageManager mgr = ConfigUsageManager.getInstance(getContext(),name);
		if( mgr != null ) {
			cache.put(name,mgr);
		}
		return mgr;
	}
	public UsageProducer getUsageProducer(){
		return getUsageManager();
	}
	public UsageProducer getUsageProducer(String name){
		
		if(name.contains(":")){
			UsageManager m = getUsageManager(name.substring(0, name.indexOf(':')));
			return m.parseProducer(name.substring(name.indexOf(':')+1));
		}else{
			boolean composite = DEFAULT_COMPOSITE_FEATURE.isEnabled(getContext());
			// try a direct implementation first
			UsageProducer up = getContext().makeObjectWithDefault(UsageProducer.class, null, name);
			if( up != null ){
				up.setCompositeHint(composite);
				return up;
			}
			ExpressionTargetFactory etf = ExpressionCast.makeExpressionTargetFactory(getContext(), name);
			if( etf != null) {
				if( etf instanceof UsageProducer) {
					return (UsageProducer) etf;
				}else {
					up = new UsageProducerWrapper(getContext(),name,etf);
					up.setCompositeHint(composite);
					return up;
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
		cache.clear();
	}
	
	public Class<? super AccountingService> getType() {
		return AccountingService.class;
	}
}