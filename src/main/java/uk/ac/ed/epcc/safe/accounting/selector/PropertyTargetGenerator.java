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
package uk.ac.ed.epcc.safe.accounting.selector;

import java.util.Date;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.Composable;

/** A {@link PropertyTargetFactory} that can also retrieve records using
 * a {@link RecordSelector}
 * 
 * @author spb
 * @param <UR> property target type
 *
 */
public interface PropertyTargetGenerator<UR> extends PropertyTargetFactory, Composable {
	
	/** Check if the specified property is defined.
	 * All properties that return true MUST be in the set returned by the
	 * getFinder call but this test can be more stringent and can omit a property if
	 * none of the produced objects can generate the specified property.
	 * 
	 * @param <P>
	 * @param tag
	 * @return boolean
	 */
	public <P> boolean hasProperty(PropertyTag<P> tag);
	
	
	/** Check if the specified property is writable
	 
	 * @param <P>
	 * @param tag
	 * @return boolean
	 */
	public <P> boolean writable(PropertyTag<P> tag);
	/** Is the RecordSelector compatible with this class. 
	 * This method will return false if it the selector is fundamentally incompatible
	 * with the properties supported by the class and no records can match the selector.
	 * It does NOT check if any matching records exist. It is expected to be a significantly
	 * more lightweight operation than calling {@link #exists(RecordSelector)}
	 * @param sel
	 * @return boolean
	 */
	default public boolean compatible(RecordSelector sel) {
		return compatible(sel, null, null);
	}
	
	/**Is the RecordSelector compatible with this class. 
	 * This method will return false if it the selector is fundamentally incompatible
	 * with the properties supported by the class and no records can match the selector.
	 * It does NOT check if any matching records exist. It is expected to be a significantly
	 * more lightweight operation than calling {@link #exists(RecordSelector)}
	 * 
	 * If the date bounds are not-null they indicate a guaranteed min/max value for all date 
	 * properties generated and can be used to short-cut the evaluation
	 * 
	 * @param sel
	 * @param start_bound
	 * @param end_bound
	 * @return
	 */
	public boolean compatible(RecordSelector sel,Date start_bound,Date end_bound);
	/** Get an Iterator over selected records. 
	
     * @param sel RecordSelector to select data
     * @param skip Number of initial records in sequence to skip
     * @param count MAximum number of records to return
     * @return  Iterator over selected records
     * @throws Exception
     */
    public abstract  CloseableIterator<UR> getIterator(RecordSelector sel,int skip, int count) throws Exception;
    /** Get an Iterator over selected records. 
	
     * @param sel RecordSelector to select data
     * @return  Iterator over selected records
     * @throws Exception
     */
    public abstract  CloseableIterator<UR> getIterator(RecordSelector sel) throws Exception;
    /** get the number of records matching the selector
     * 
     * @param selector
    
    
     * @return number o matches
     * @throws Exception 
     */
    public long getRecordCount(RecordSelector selector) throws Exception;
    
    /** check if ANY records match the selector. This is more definative but more expensive than
     * {@link #compatible(RecordSelector)}.
     * 
     * @param selector
     * @return
     * @throws Exception
     */
    public boolean exists(RecordSelector selector) throws Exception;
    
   	/**
   	 * Gets the distinct property values for the set of records identified by the RecordSelector
   	 * @param <PT> 
   	 * 
   	 * @param data_tag the data_tag
   	 * @param selector the selector
   	 * 
   	 * @return Set of property values
   	 * 
   	 * @throws Exception 
   	 */
   	public <PT> Set<PT> getValues(PropExpression<PT> data_tag, RecordSelector selector) 
   		throws Exception;
   	
}