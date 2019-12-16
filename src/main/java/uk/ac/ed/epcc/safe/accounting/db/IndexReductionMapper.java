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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.IllegalReductionException;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.GeneralMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.GroupingSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.InvalidKeyException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.ResultMapper;


/** A {@link ResultMapper} configured by {@link ReductionTarget}s
 * 
 * @author spb
 *
 * @param <T>
 */
public class IndexReductionMapper<T> extends GeneralMapMapper<ExpressionTuple, ReductionMapResult>{
		private Set<ReductionTarget> sum;
		private Set<ReductionTarget> skip;
		private Map<ReductionTarget,Object> default_map;
		//
		// This used to be the behaviour we may need to re-instate if old reports break.
		//
		private static final Feature SELECTS_IN_KEY = new Feature("reporting.index_reduction.add_selects_to_key",false,"Include select clauses in IndexedReduction index");
		@SuppressWarnings("unchecked")
		public IndexReductionMapper(AccessorMap map, Set<ReductionTarget> targets,ReductionMapResult defs) throws InvalidPropertyException, IllegalReductionException, CannotUseSQLException{
			super(map.getContext());
			int indexes=0;
			int reductions=0;
			int skips=0;
			default_map=defs;
			// Make sure iteration order is consistent
			this.sum=new LinkedHashSet<>(targets);
			//Logger log = getContext().getService(LoggerService.class).getLogger(getClass());
			//TODO consider using a wider variety of exceptions here.
			for(ReductionTarget target : sum){
				PropExpression<?> t = target.getExpression();
				SQLValue<?> val = null;
				try{
					try{
						val = map.getSQLExpression(t);
					}catch(InvalidSQLPropertyException ee){
						// fall back to a SQLValue
						val = map.getSQLValue(t);
					}
				}catch(InvalidSQLPropertyException e){
					if( map.resolves(t, false) ){
						// This must be implemented via an Accessor
						throw new CannotUseSQLException("property not reachable from SQL "+t.toString());
					}
					if( target.getReduction() == Reduction.INDEX){
						// can't skip an index
						throw e;
					}
					// the expression does not resolve
					if( skip == null ){
						skip = new HashSet<>();
					}
					skip.add(target);
					skips++;
				}
				if( val != null ){
					String value_name=null;
					// Seems safer to just skip all custom names.
					//if( ! (val instanceof FieldValue)){
					//	value_name=t.toString();
					//}
					if( target.getReduction() == Reduction.INDEX || (target.getReduction()==Reduction.SELECT && SELECTS_IN_KEY.isEnabled(getContext()))){
						if( val instanceof GroupingSQLValue) {
							try {
								addKey((GroupingSQLValue<?>) val, value_name);
							} catch (InvalidKeyException e) {
								throw new CannotUseSQLException("value "+val.toString()+" cannot be used in SQL GROUP BY");
							}
							indexes++;
						}else {
							throw new CannotUseSQLException("value "+val.toString()+" cannot be used in SQL GROUP BY");
						}
					}else{
						if( val instanceof SQLExpression){
							if( Number.class.isAssignableFrom(val.getTarget())){
								SQLExpression<? extends Number> e = (SQLExpression<? extends Number>) val;


								//log.debug("reduction on number property "+t+" maps to "+e);
								switch(target.getReduction()){
								case SUM: addSum(e,value_name);break;
								case MIN: addMinNumber(e, value_name);break;
								case MAX: addMaxNumber(e, value_name);break;
								case AVG: addAverage(e, value_name);break;
								case SELECT:addClause(val, value_name);break; 
								case DISTINCT: addCount(e, value_name); break; // might be counting distinct numbers
								case MEDIAN: throw new CannotUseSQLException("Medians cannot be calculated using SQL");
								default: throw new IllegalReductionException("Bad number reduction");
								}
							}else if( Date.class.isAssignableFrom(val.getTarget()) ){
								SQLExpression<? extends Date> e = (SQLExpression<? extends Date>) val;


								//log.debug("reduction on date property "+t+" maps to "+e);
								switch(target.getReduction()){
								case MIN: addMinDate(e, value_name);break;
								case MAX: addMaxDate(e, value_name);break;
								case SELECT:addClause(val, value_name);break; 
								case DISTINCT: addCount(e, value_name); break; // might be counting distinct times
								default: throw new IllegalReductionException("Bad date reduction");
								}

							}else{
								switch(target.getReduction()) {
								case SELECT:addClause(val, value_name);break; 
								case DISTINCT: addCount(val, value_name); break; 
								default: throw new IllegalReductionException("Unsupported data type for reduction");
								}
							}
						}else{
							switch(target.getReduction()) {
							case SELECT:addClause(val, value_name); break;
							case DISTINCT: addCount(val, value_name); break;
							default: throw new CannotUseSQLException("Not an SQL Expression");
							}
						}
						reductions++;
					}
				}
			}
			if( reductions == 0 && skips >0 ){
				// This might be a problem as spurious index values might be generated.
				getLogger().error("All reductions skipped!");
			}
		}

		@Override
		protected ExpressionTuple makeKey(ResultSet rs) throws DataException, SQLException {
			Map<PropExpression,Object> map = new HashMap<>();
			int pos=0;  // This is expression count NOT fields super-class keeps track
			for(ReductionTarget target : sum){
		
					if( target.getReduction() == Reduction.INDEX){

						PropExpression tag =  target.getExpression();
						map.put(tag, getTargetObject(pos, rs));
					}
					pos++;

			}
			
			return new ExpressionTuple(map);
			
		}

		@Override
		protected ReductionMapResult makeResult(ResultSet rs) throws DataException, SQLException {
			ReductionMapResult map = new ReductionMapResult();
			int pos=0; // This is expression count not fields superclass keeps track
			for(ReductionTarget t : sum){
				if( skip != null && skip.contains(t)){
					Object def = t.getDefault();
					if( def != null ){
						map.put(t,def);
					}
				}else{ 
					Object n = getTargetObject(pos,rs);
					if( n == null && default_map != null){
						n = default_map.get(t);
					}
					map.put(t, n);
					pos++;
				}
			}
			return map;
		}
		/* (non-Javadoc)
		 * @see uk.ac.ed.epcc.webapp.jdbc.filter.MapMapper#combine(java.lang.Object, java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected ReductionMapResult combine(ReductionMapResult a, ReductionMapResult b) {
			if (a == null) {
				return b;
			}
			if (b == null) {
				return a;
			}
			// Loop over entries in b and merge into a. This ensures
			// that entries in a single result are merged
			// do NOT use sum as this may contain duplicates.
			for(ReductionTarget target : b.keySet()){
				Object value_a = a.get(target);
				Object value_b = b.get(target);
				a.put(target, target.combine(value_a, value_b));
			}
			return a;
		}
		
	}