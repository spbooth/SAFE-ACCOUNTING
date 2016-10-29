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
package uk.ac.ed.epcc.safe.accounting;


import uk.ac.ed.epcc.safe.accounting.db.GeneratorReductionHandler;
import uk.ac.ed.epcc.safe.accounting.db.ReductionHandler;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
import uk.ac.ed.epcc.webapp.Tagged;

/** Interface implemented by classes that can provide Usage information to the reports.
 * 
 * This interface is intended to be implementable by composite objects that combine multiple tables.
 * 
 * @see ReductionHandler
 * @see GeneratorReductionHandler
 * @author spb
 * @param <UR> 
 *
 */
public interface UsageProducer<UR extends UsageRecord> extends 
ExpressionTargetGenerator<UR>, 
Tagged, 
ReductionProducer<UR>{

	
	
	/** Does this class support the specified property. A false return 
	 * value is a definitive statement that the property will not be available in any records 
	 * this UsageProducer retrieves from the database though the properties may exist during the
	 * parse phase.
	 * 
	 * A true return value indicates that some of the records returned may contain the specified property.
	 * Also the meanings from {@link PropertyTargetFactory}
	 * 
	 * @param tag
	 * @return boolean
	 */
	@Override
	public <X> boolean hasProperty(PropertyTag<X> tag);
	
	    
    
	

}