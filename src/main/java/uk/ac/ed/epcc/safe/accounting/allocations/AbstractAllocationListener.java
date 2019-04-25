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

import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;

public class AbstractAllocationListener<T extends AllocationFactory.AllocationRecord> implements AllocationListener<T>{

	public void deleted(T rec) {
	
		
	}

	public void canCreate(PropertyContainer values) throws ListenerObjection {
		
		
	}

	public void created(T rec) {
	
		
	}

	public void canModify(T rec, PropertyContainer values)
			throws ListenerObjection {
		
		
	}

	public void modified(T rec, String details) {
		
		
	}

	public void split(T before, T after) {
		
		
	}

	public void canMerge(T before, T after) throws ListenerObjection {
	
		
	}

	public void merge(T before, T after) {
		
		
	}

	public void aggregate(T rec, PropertyContainer props, boolean add) {
		
	}

}