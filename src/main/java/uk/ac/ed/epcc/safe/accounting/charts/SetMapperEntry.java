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
package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.Date;
import java.util.Vector;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.strategy.SetRangeMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
/** A MapperEntry that generates a single fixed dataset.
 * 
 * 
 * @author spb
 *
 */


public class SetMapperEntry extends MapperEntry {
	private static final int set=0;
	private String label=null;
	public  SetMapperEntry(AppContext c,String name,String mode,String description){
    	super(c,name,mode,description);
    }
	public void setLabel(String lab){
		label=lab;
	}
	public String getLabel() {
		return label;
	}
	@SuppressWarnings("unchecked")
	public SetRangeMapper getMapper(PlotEntry e) {
    	
    	return new UsageRecordSetTransform(conn,set,e.getReduction(),e.getPlotProperty(),e.getStartProperty(),e.getEndProperty());

    }
	@SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getPointQueryMapper(RecordSelector sel,
			Reduction red,PropExpression prop_tag, PropExpression<Date> end_prop) {
		return new PointUsageRecordQueryMapper(getContext(), sel,set,red,prop_tag,end_prop,null);
				
	}
	@SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getOverlapQueryMapper(
			RecordSelector s,Reduction red,
			PropExpression<? extends Number> prop_tag, PropExpression<Date> start_prop,
			PropExpression<Date> end_prop,long cutoff) {
		return new OverlapUsageRecordQueryMapper(getContext(),s,set,red,prop_tag,start_prop,end_prop,cutoff,null);
	}
	@SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getInnerQueryMapper(RecordSelector sel,
			Reduction red,PropExpression<? extends Number> prop_tag,
			PropExpression<Date> start_prop, PropExpression<Date> end_prop,long cutoff) {
		return new InnerUsageRecordQueryMapper(getContext(),sel,set,red,prop_tag,start_prop,end_prop,cutoff,null);
	}
	@Override
	protected Vector<String> getLabels() {
		if( label == null ){
			return null;
		}
		Vector<String> result = new Vector<>();
		result.add(label);
		return result;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		SetMapperEntry other = (SetMapperEntry) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
}