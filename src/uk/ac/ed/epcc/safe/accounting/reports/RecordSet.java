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
import uk.ac.ed.epcc.safe.accounting.db.NullUsageProducer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.RecordSelectException;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.preferences.Preference;
import uk.ac.ed.epcc.webapp.time.Period;

/** RecordSet combines a combination of {@link UsageProducer} and {@link RecordSelector}
 * and time bounds (Array of {@link PropExpression}s) it represents a set of UsageRecords.
 * The time bounds are used to generate {@link PeriodOverlapRecordSelector}s. These are not stored directly in
 * the {@link RecordSelector} as they will depend on the time period under consideration.
 * 
 * This is required where reports need a combination of information from
 * incompatible {@link UsageProducer}s. For example Data usage and allocations. 
 * 
 * @author spb
 *
 */


public class RecordSet extends ObjectSet<UsageProducer>{
	private static final Feature NARROW_PRODUCER_IN_RECORDSET = new Preference("reports.narrow_producer_in_recordset",true,"Narrow composite producers in recordset");

	private final AccountingService serv;
  private PropExpression<Date> bounds[];
  private boolean use_overlap=true;
  private UsageProducer narrowed=null;
  private boolean use_narrowed=false;
 
  public RecordSet(AccountingService serv){
	  super();
	  this.serv=serv;
	  bounds = new PropExpression[1];
	  bounds[0]=StandardProperties.ENDED_PROP;
  }
  public RecordSet(RecordSet orig){
	  super(orig);
	  this.serv=orig.serv;
	  if( orig.bounds != null ) {
		  this.bounds=orig.bounds.clone();
	  }
  }
  public UsageProducer getUsageProducer(){
	  if( NARROW_PRODUCER_IN_RECORDSET.isEnabled(serv.getContext())) {
		  if( use_narrowed ) {
			  return narrowed;
		  }else {
			  try {
				  // We could apply a time filter here but
				  // safer if we don't in case time bounds change later
				  // wrap the selector so
				  narrowed = getGenerator().narrow(getRecordSelector().copy());
//				  if( narrowed == null) {
//					  narrowed=new NullUsageProducer<>(serv.getContext());
//				  }
				  use_narrowed=true;
				  return narrowed;
			  } catch (Exception e) {
				  getLogger().error("Error narrowing UsageProducer", e);
			  }

		  }
	  }
	  return getGenerator();
  }
/**
 * @return
 */
public Logger getLogger() {
	return serv.getContext().getService(LoggerService.class).getLogger(getClass());
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
	/** Returns a {@link RecordSelector} including the filter selector and a selector based on the 
	 * time bounds and the specified {@link Period}.
	 * 
	 * @param period
	 * @return
	 * @throws RecordSelectException 
	 */
	public AndRecordSelector getPeriodSelector(Period period) throws RecordSelectException{
		AndRecordSelector sel = new AndRecordSelector(getRecordSelector());
		if( bounds == null ) {
			throw new RecordSelectException("No time bounds set");
		}
		ExpressionTargetGenerator<?> up = getGenerator();
		if( bounds.length == 1 && up.compatible(bounds[0])){
				 sel.add(new PeriodOverlapRecordSelector(period, bounds[0]));
		}else if( hasOverlapBounds() && up.compatible(bounds[0]) && up.compatible(bounds[1])){
			sel.add(new PeriodOverlapRecordSelector(period, bounds[0], bounds[1]));
		}else {
			throw new RecordSelectException("No valid time bounds");
		}
		return sel;
	}
	/** Do we have the bounds to calculate an overlap filter
	 * @param up
	 * @return
	 */
	private boolean hasOverlapBounds() {
		ExpressionTargetGenerator<?> up = getGenerator();
		return bounds != null && bounds.length == 2 && up.compatible(bounds[0]) && up.compatible(bounds[1]);
	}
	/** Are overlap calculations requested
	 * @param up
	 * @return
	 */
	public boolean useOverlap() {
		//return use_overlap && bounds.length == 2 && up.compatible(bounds[0]) && up.compatible(bounds[1]);
		return use_overlap && hasOverlapBounds() ;
	}
  public void setUsageProducer(String name) throws RecordSelectException{
	  UsageProducer usageProducer = serv.getUsageProducer(name);
	  if( usageProducer == null) {
		  throw new RecordSelectException("Bad producer: "+name);
	  }
	setUsageProducer(usageProducer);
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
	result = prime * result + (use_overlap ? 0 : 1);  // Boolean.hashCode requires java-8
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
@Override
protected void clearCaches() {
	use_narrowed=false;
	narrowed=null;
}
public PropertyFinder getFinder() {
	return getGenerator().getFinder();
}
}