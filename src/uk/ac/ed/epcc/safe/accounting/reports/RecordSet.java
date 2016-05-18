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
package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.Arrays;
import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.time.Period;

/** RecordSet combines a combination of {@link UsageProducer} and {@link RecordSelector}
 * and time bounds (Array of {@link PropExpression}s) it represents a set of UsageRecords.
 * The time bounds are used to generate {@link PeriodOverlapRecordSelector}s. These are not stored directly in
 * the {@link RecordSelector} as they wil depend on the time period under consideration.
 * 
 * This is required where reports need a combination of informations from
 * incompatible {@link UsageProducer}s. For example Data usage and allocations. 
 * 
 * @author spb
 *
 */


public class RecordSet extends ObjectSet<UsageProducer>{
  
private final AccountingService serv;
  private PropExpression<Date> bounds[];
  private boolean use_overlap=true;
 
  public RecordSet(AccountingService serv){
	  super();
	  this.serv=serv;
	  bounds = new PropExpression[1];
	  bounds[0]=StandardProperties.ENDED_PROP;
  }
  public RecordSet(RecordSet orig){
	  super(orig);
	  this.serv=orig.serv;
	  this.bounds=orig.bounds.clone();
  }
  public UsageProducer getUsageProducer(){
	  return getGenerator();
  }
  public void setBounds(PropExpression<Date> bounds[]){
		this.bounds=bounds;
	}
    public void setUseOverlap(boolean val){
    	this.use_overlap=val;
    }
	public PropExpression<Date>[] getBounds(){
		return bounds;
	}
	/** Returns a {@link RecordSelector} including a selector based on the 
	 * time bounds and the specified {@link Period}.
	 * 
	 * @param period
	 * @return
	 */
	public AndRecordSelector getPeriodSelector(Period period){
		AndRecordSelector sel = new AndRecordSelector(getRecordSelector());
		ExpressionTargetGenerator<?> up = getGenerator();
		if( bounds.length == 1 && up.compatible(bounds[0])){
			sel.add(new PeriodOverlapRecordSelector(period, bounds[0]));
		}else if( hasOverlapBounds()){
			sel.add(new PeriodOverlapRecordSelector(period, bounds[0], bounds[1]));
		}
		return sel;
	}
	/** Do we have the bounds to calculate an overlap filter
	 * @param up
	 * @return
	 */
	private boolean hasOverlapBounds() {
		ExpressionTargetGenerator<?> up = getGenerator();
		return bounds.length == 2 && up.compatible(bounds[0]) && up.compatible(bounds[1]);
	}
	/** Are overlap calculations requested
	 * @param up
	 * @return
	 */
	public boolean useOverlap() {
		ExpressionTargetGenerator<?> up = getGenerator();
		return use_overlap && bounds.length == 2 && up.compatible(bounds[0]) && up.compatible(bounds[1]);
	}
  public void setUsageProducer(String name){
	  setUsageProducer(serv.getUsageProducer(name));
  }
  public void setUsageProducer(UsageProducer up){
	  // Note that setting a usage producer clear all existing selectors.
	  setGenerator(up);
	  clearSelection();
	  
  }
@Override
public UsageProducer getGenerator() {
	UsageProducer up = super.getGenerator();
	  if( up == null ){
		  // Use default
		  up=serv.getUsageProducer();
		  setGenerator(up);
	  }
	  return up;
}
@Override
public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + Arrays.hashCode(bounds);
	result = prime * result + Boolean.hashCode(use_overlap);
	return result;
}
@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (!super.equals(obj))
		return false;
	if (getClass() != obj.getClass())
		return false;
	RecordSet other = (RecordSet) obj;
	if (!Arrays.equals(bounds, other.bounds))
		return false;
	if( use_overlap != other.use_overlap){
		return false;
	}
	return true;
}
}