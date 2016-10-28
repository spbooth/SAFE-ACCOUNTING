//| Copyright - The University of Edinburgh 2016                            |
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
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.webapp.jdbc.expr.Accessor;
import uk.ac.ed.epcc.webapp.time.TimePeriod;

/** An extended version of {@link Accessor} that can evaluate in a time context 
 * 
 * This might be needed if the Accessor is really generating a sub-query parameterised 
 * by the target object and subject to the time window. Rather than calculating the overlap with 
 * the parent record then rescaling by the overlap with the time window it can directly calculate the
 * overlap with the intersection period. 
 * 
 * The canonical value of the accessor should use the time extent of the target object.
 * 
 *  The {@link #getValue(Object)} method should always return the same value as passing the target 
 *  to both arguments of {@link #getOverlap(TimePeriod, TimePeriod)}
 * 
 * 
 * @author spb
 *
 */
public interface TimePeriodAccessor<T,R extends TimePeriod> extends Accessor<T, R> {

	/** calculate the value that overlaps with the specified time period
	 * 
	 * @param period TimePeriod of interest
	 * @param r target object
	 * @return
	 */
	public T getOverlap(TimePeriod period, R r);
	
}
