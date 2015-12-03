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
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.BarTimeChart;
import uk.ac.ed.epcc.webapp.charts.SingleValueSetPlot;
import uk.ac.ed.epcc.webapp.charts.strategy.SetRangeMapper;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.Labeller;

/** A MapperEntry that generates multiple plot sets depending on 
 * a key property
 * 
 * @author spb
 *
 * @param <K>
 */


public class KeyMapperEntry<K> extends MapperEntry {

    private final PropExpression<K> key_tag;
   
    private final PropertyKeyLabeller pkl; // need to use the same labeller for all data adds

    @SuppressWarnings("unchecked")
	public  KeyMapperEntry(AppContext conn,PropExpression<K> key,Labeller<K,?> lab,String name,String description){
    	super(conn,name,description);
    	Labeller labeller;
    	if( lab != null ){
    		key_tag=key;
    	    labeller=lab;
    	}else{
    		if( key instanceof FormatProvider){
    			key_tag=key;
    			labeller=((FormatProvider) key).getLabeller();
    		}else{
    			key_tag=key;
    			labeller=null;
    		}
    	}
    	pkl = new PropertyKeyLabeller<K>(conn,key_tag,labeller);
    }
    
    @SuppressWarnings("unchecked")
	public SetRangeMapper getMapper(PlotEntry e) {
    	   return new UsageRecordTransform(conn,e.getReduction(),key_tag,e.getPlotProperty(),e.getStartProperty(),e.getEndProperty(),pkl);
    }
    @SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getPointQueryMapper(RecordSelector sel,
			Reduction red,
			PropExpression<? extends Number> prop_tag, PropertyTag<Date> end_prop) {
		return new PointUsageRecordQueryMapper(getContext(), sel,key_tag,red,prop_tag,end_prop,pkl);
	}
    @SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getOverlapQueryMapper(
			RecordSelector s,
			Reduction red,
			PropExpression<? extends Number> prop_tag, PropertyTag<Date> start_prop,
			PropertyTag<Date> end_prop) {
		return new OverlapUsageRecordQueryMapper(getContext(),s,key_tag,red,prop_tag,start_prop,end_prop,pkl);
	}
    @SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getInnerQueryMapper(RecordSelector sel,
			Reduction red,
			PropExpression<? extends Number> prop_tag,
			PropertyTag<Date> start_prop, PropertyTag<Date> end_prop) {
		return new InnerUsageRecordQueryMapper(getContext(),sel,key_tag,red,prop_tag,start_prop,end_prop,pkl);
	}
    @SuppressWarnings("unchecked")
	@Override
    protected Vector<String> getLabels(){
    	if( pkl != null ){
        	return pkl.getLabels();
        }
    	return null;
    }
    @Override
    public boolean compatible(UsageProducer<?> ap){
    	return  ap.compatible(key_tag);
    }

	@Override
	public void plotBarTimeChart(PlotEntry e, BarTimeChart tc, int nplots,
			SingleValueSetPlot ds) {
		super.plotBarTimeChart(e, tc, nplots, ds);
		if( nplots == 0){
			// use natural ordering of keys.
			// Have to sort labels as well
			int perm[] = pkl.getPerm();
			ds.setLegends((String[])pkl.getLabels().toArray(new String[0]));
			ds.permSets(perm.length, perm);
			
		}
	}
}