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
package uk.ac.ed.epcc.safe.accounting;

import java.util.LinkedHashMap;
import java.util.Map;

/** Result of a multiple reduction.
 * This is a map from the Requested ReductionTarget to the result
 * 
 * @author spb
 *
 */


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