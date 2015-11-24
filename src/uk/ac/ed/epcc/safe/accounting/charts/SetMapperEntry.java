// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.Date;
import java.util.Vector;

import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.strategy.SetRangeMapper;
/** A MapperEntry that generates a single fixed dataset.
 * 
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: SetMapperEntry.java,v 1.6 2014/09/15 14:32:19 spb Exp $")

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
			Reduction red,PropExpression<? extends Number> prop_tag, PropertyTag<Date> end_prop) {
		return new PointUsageRecordQueryMapper(getContext(), sel,set,red,prop_tag,end_prop,null);
				
	}
	@SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getOverlapQueryMapper(
			RecordSelector s,Reduction red,
			PropExpression<? extends Number> prop_tag, PropertyTag<Date> start_prop,
			PropertyTag<Date> end_prop) {
		return new OverlapUsageRecordQueryMapper(getContext(),s,set,red,prop_tag,start_prop,end_prop,null);
	}
	@SuppressWarnings("unchecked")
	protected UsageRecordQueryMapper getInnerQueryMapper(RecordSelector sel,
			Reduction red,PropExpression<? extends Number> prop_tag,
			PropertyTag<Date> start_prop, PropertyTag<Date> end_prop) {
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