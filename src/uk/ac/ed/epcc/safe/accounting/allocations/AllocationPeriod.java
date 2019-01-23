//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.time.Period;

public class AllocationPeriod implements PropertyTarget{

	private final Period period;
	private final PropertyMap index;
	public AllocationPeriod(ViewPeriod period) {
		this(period,new PropertyMap());
	}
	public AllocationPeriod(Period period, PropertyMap map){
		this.period=period;
		this.index = map;
	}

	public PropertyMap getIndex(){
		return index;
	}
	public Period getPeriod(){
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