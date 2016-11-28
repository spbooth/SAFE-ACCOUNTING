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

import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.strategy.SetRangeMapper;
/** A MapperEntry that generates a single fixed dataset.
 * 
 * 
 * @author spb
 *
 */


public class SetMapperEntry extends MapperEntry {
	private static final int set=0;
	private String label=null;
	public  SetMapperEntry(AppContext c,String name,String description){
    	super(c,name,description);
    }
	public void setLabel(String lab){
		label=lab;
	}
	@SuppressWarnings("unchecked")
	public SetRangeMapper getMapper(PlotEntry e) {
    	
    	return new UsageRecordSetTransform(conn,set,e.getReduction(),e.getPlotProperty(),e.getStartProperty(),e.getEndProperty());

    }
	@SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getPointQueryMapper(RecordSelector sel,
			Reduction red,PropExpression<? extends Number> prop_tag, PropExpression<Date> end_prop) {
		return new PointUsageRecordQueryMapper(getContext(), sel,set,red,prop_tag,end_prop,null);
				
	}
	@SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getOverlapQueryMapper(
			RecordSelector s,Reduction red,
			PropExpression<? extends Number> prop_tag, PropExpression<Date> start_prop,
			PropExpression<Date> end_prop) {
		return new OverlapUsageRecordQueryMapper(getContext(),s,set,red,prop_tag,start_prop,end_prop,null);
	}
	@SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getInnerQueryMapper(RecordSelector sel,
			Reduction red,PropExpression<? extends Number> prop_tag,
			PropExpression<Date> start_prop, PropExpression<Date> end_prop) {
		return new InnerUsageRecordQueryMapper(getContext(),sel,set,red,prop_tag,start_prop,end_prop,null);
	}
	@Override
	protected Vector<String> getLabels() {
		if( label == null ){
			return null;
		}
		Vector<String> result = new Vector<String>();
		result.add(label);
		return result;
	}
}