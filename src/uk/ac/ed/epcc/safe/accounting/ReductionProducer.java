package uk.ac.ed.epcc.safe.accounting;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTuple;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;

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