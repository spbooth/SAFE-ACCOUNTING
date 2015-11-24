// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting;

import java.util.LinkedHashMap;
import java.util.Map;

/** Result of a multiple reduction.
 * This is a map from the Requested ReductionTarget to the result
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ReductionMapResult.java,v 1.5 2014/09/15 14:32:18 spb Exp $")

public class ReductionMapResult extends LinkedHashMap<ReductionTarget,Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6784951741311253045L;

	public ReductionMapResult() {
		super();
	}

	public ReductionMapResult(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public ReductionMapResult(int initialCapacity) {
		super(initialCapacity);
	}

	public ReductionMapResult(Map<? extends ReductionTarget, ? extends Object> m) {
		super(m);
	}

}