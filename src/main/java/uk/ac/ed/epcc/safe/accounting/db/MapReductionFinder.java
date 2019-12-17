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
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Date;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.IllegalReductionException;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.AverageMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.CountDistinctMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.GroupingSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.InvalidKeyException;
import uk.ac.ed.epcc.webapp.jdbc.expr.MapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.MaximumDateMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.MaximumMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.MinimumDateMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.MinimumMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.SumMapMapper;
import uk.ac.ed.epcc.webapp.model.data.FieldValue;



public class MapReductionFinder<T,K,R,N> extends AccessorMapFilterFinder<T, Map<K,R>> {
	public MapReductionFinder(AccessorMap<T> map, PropExpression<K> key,
			ReductionTarget<R,N> value) throws InvalidSQLPropertyException, IllegalReductionException {
		this(map,key,value,false);
	}
	public MapReductionFinder(AccessorMap<T> map, PropExpression<K> key,
			ReductionTarget<R,N> value, boolean composite) throws InvalidSQLPropertyException, IllegalReductionException {
		super(map); // can return null
		assert(key != null);
		assert(value != null);
		
		SQLValue<K> v = map.getSQLValue(key);
		if (! (v instanceof GroupingSQLValue)) {
			// has to be a grouping value
			throw new InvalidSQLPropertyException(key);
		}
		
		GroupingSQLValue<K> a = (GroupingSQLValue<K>) v;
		if( ! a.groupingIsomorphic() ) {
			composite = true;
		}
		String key_name=null;
		if( ! (a instanceof FieldValue)){
			key_name = key.toString();
		}
		
		SQLExpression<? extends N> val  = map.getSQLExpression(value.getExpression());
		String value_name=null;
		if( ! (val instanceof FieldValue)){
			value_name=value.toString();
		}
		try {
			MapMapper mapper = new MapMapper(map.getContext(), a, key_name);
			if( Number.class.isAssignableFrom(val.getTarget())){
				SQLExpression<? extends Number> e = (SQLExpression<? extends Number>) val;
				
				switch(value.getReduction()){
				case SUM: mapper.addSum(e,value_name); break;
				case MIN: mapper.addMinNumber(e,value_name); break;
				case MAX: mapper.addMaxNumber(e,value_name); break;
				case AVG:
					if( composite ) {
						mapper.addAverage(e,value_name);
					}else {
						mapper.addSQLAverage(e, value_name); 
					}
					break;
				case DISTINCT: 
					if( composite ) {
						mapper.addCount(e, value_name); 
					}else{
						mapper.addSQLCount(e, value_name);
					}
					break;
				}
				
			}else if( Date.class.isAssignableFrom(val.getTarget()) ){
				SQLExpression<? extends Date> e = (SQLExpression<? extends Date>) val;
				switch(value.getReduction()){
				
				case MIN: mapper.addMinDate(e, value_name); break;
				case MAX: mapper.addMaxDate(e, value_name); break;
				case DISTINCT: 
					if( composite ) {
						mapper.addCount(e, value_name); 
					}else{
						mapper.addSQLCount(e, value_name);
					}
					break;
				default: throw new IllegalReductionException("Bad date reduction");
				}
			}else {
				switch(value.getReduction()){
				case DISTINCT: 
					if( composite ) {
						mapper.addCount(val, value_name); 
					}else{
						mapper.addSQLCount(val, value_name);
					}
					break;
				default: throw new IllegalReductionException("Unsupported data type for reduction");
				}
			}
			setMapper(mapper);
		}catch(InvalidKeyException e1) {
			throw new InvalidSQLPropertyException(key);
		}
	}
}