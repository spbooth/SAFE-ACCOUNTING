package uk.ac.ed.epcc.safe.accounting.selector;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
/** Select records that are compatible with a set of {@link ReductionTarget}s
 * Index reductions are required to be generated put at least one of any
 * remaining targets.
 * @author spb
 *
 */
public class ReductionSelector extends HashSet<ReductionTarget> implements RecordSelector {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1637334986321111729L;

	public ReductionSelector(Set<ReductionTarget> list) {
		super(list);
	}

	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		return visitor.visitReductionSelector(this);
	}

	public RecordSelector copy() {
		return (RecordSelector) this.clone();
	}
}
