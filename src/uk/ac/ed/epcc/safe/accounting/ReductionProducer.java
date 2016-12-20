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
package uk.ac.ed.epcc.safe.accounting;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.ReductionHandler;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTuple;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
/** Interface for class that supports reductions.
 * 
 * Any {@link ExpressionTargetFactory} can implement this interface by using a {@link ReductionHandler}
 * 
 * @author spb
 *
 * @param <UR>
 */
public interface ReductionProducer<UR extends ExpressionTarget> {

	/** Perform a reduction over a set of records
	 * 
	 * @param target ReductionTarget specifying reduction to perform
	 * @param sel RecordSelector to select target records
	 * @return result of reduction
	 * @throws Exception 
	 */
	public abstract <T> T getReduction(ReductionTarget<T> target,
			RecordSelector sel) throws Exception;

	//   /** Get a map between {@link PropertyTag} and the values they generate.
	//    *  
	//    *  If the selected records have more than one mapping then the results are
	//    *  undefined.
	//    * 
	//    * @param <I>
	//    * @param <P>
	//    * @param index
	//    * @param property
	//    * @param selector
	//    * @return Map
	//    * @throws Exception
	//    */
	//    @Deprecated
	//    public <I, P> Map<I, P> getPropMap(PropertyTag<I> index, PropertyTag<P> property, RecordSelector selector) throws Exception;
	/** General reduction operation. 
	 * The reduction is specified by a set of {@link ReductionTarget} objects. This method generates
	 * Maps from the requested {@link ReductionTarget} to the result of the corresponding reduction.
	 * If the list of ReductionTargets contains any {@link IndexReduction} objects then multiple results will
	 * be returned so these results are themselves returned as a Map indexed by {@link PropertyTuple} objects
	 * representing the Index values. If there are no IndexValues an empty PropertyTuple will be used.
	 *
	 * A non-index target that does not resolve may be ignored or return the default value for
	 * the {@link ReductionTarget}. This is so target set compatible with composite {@link UsageProducer}s 
	 * can contain targets only relevant to some of its parts without suppressing all output from the
	 * tables that don't support the property. This does run the risk that spurious index-tuples
	 * might be generated from tables that support none of the non-index targets.
	 * In that case the producer has to be tuned.
	 * @param property
	 * @param selector
	 * @return Map
	 * @throws Exception 
	 */
	public abstract Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap(
			Set<ReductionTarget> property, RecordSelector selector)
			throws Exception;

	/** sum the specified numerical quantity over all records that match the selector
	 * grouped by the other specified property
	 * @param <I>
	
	 * @param index
	 * @param property
	 * @param selector
	 * @return Map
	 * @throws Exception 
	 */

	public abstract <I> Map<I, Number> getReductionMap(PropExpression<I> index,
			ReductionTarget<Number> property, RecordSelector selector)
			throws Exception;

}