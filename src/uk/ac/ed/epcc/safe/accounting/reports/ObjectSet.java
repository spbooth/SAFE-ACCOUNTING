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
package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;

/** This is a combination of an {@link ExpressionTargetGenerator}
 * and a {@link RecordSelector}
 * 
 * @author spb
 * @param <F> type of {@link ExpressionTargetGenerator}
 *
 */
public class ObjectSet<F extends ExpressionTargetGenerator> {
	private F generator=null;
	private AndRecordSelector sel=new AndRecordSelector();
	
	
	protected ObjectSet(){
		
	}
	public ObjectSet(ObjectSet<F> orig){
		this.generator=orig.getGenerator();
		this.sel = new AndRecordSelector(orig.getRecordSelector().copy());
	}
	

	public F getGenerator() {
		return generator;
	}
	public void setGenerator(F generator) {
		this.generator = generator;
	}

	public final  RecordSelector getRecordSelector(){
		return sel;
	}
	protected void clearSelection(){
		sel=new AndRecordSelector();
	}
	public final void addRecordSelector(RecordSelector sel){
		this.sel.add(sel);
	}
	@Override
	public final String toString(){
		return "["+generator==null?"no-generator":generator.toString()+","+sel==null?"no-selector":sel.toString()+"]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((generator == null) ? 0 : generator.hashCode());
		result = prime * result + ((sel == null) ? 0 : sel.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectSet other = (ObjectSet) obj;
		if (generator == null) {
			if (other.generator != null)
				return false;
		} else if (!generator.equals(other.generator))
			return false;
		if (sel == null) {
			if (other.sel != null)
				return false;
		} else if (!sel.equals(other.sel))
			return false;
		return true;
	}

}