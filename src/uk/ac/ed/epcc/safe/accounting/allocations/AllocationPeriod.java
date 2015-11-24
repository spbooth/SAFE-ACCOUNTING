package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;

public class AllocationPeriod implements PropertyTarget{

	private final ViewPeriod period;
	private final PropertyMap index;
	public AllocationPeriod(ViewPeriod period) {
		this(period,new PropertyMap());
	}
	public AllocationPeriod(ViewPeriod period, PropertyMap map){
		this.period=period;
		this.index = map;
	}

	public PropertyMap getIndex(){
		return index;
	}
	public ViewPeriod getPeriod(){
		return period;
	}
	@Override
	public <T> T getProperty(PropertyTag<T> tag, T def) {
		if( tag == StandardProperties.STARTED_PROP){
			return (T) period.getStart();
		}else if( tag == StandardProperties.ENDED_PROP){
			return (T) period.getEnd();
		}
		return index.getProperty(tag, def);
	}
	

}
