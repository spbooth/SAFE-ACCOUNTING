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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.IllegalReductionException;
import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.GeneralMapMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.FieldValue;



public class IndexReductionMapper<T extends DataObject&ExpressionTarget> extends GeneralMapMapper<ExpressionTuple, ReductionMapResult>{
		private Set<ReductionTarget> sum;
		private Set<ReductionTarget> skip;
		private Map<ReductionTarget,Object> default_map;
		//
		// This used to be the behaviour we may need to re-instate if old reports break.
		//
		private static final Feature SELECTS_IN_KEY = new Feature("reporting.index_reduction.add_selects_to_key",false,"Include select clauses in IndexedReduction inded");
		@SuppressWarnings("unchecked")
		public IndexReductionMapper(AccessorMap map, Set<ReductionTarget> targets,ReductionMapResult defs) throws InvalidPropertyException, IllegalReductionException, CannotUseSQLException{
			super(map.getContext());
			int indexes=0;
			int reductions=0;
			int skips=0;
			default_map=defs;
			// Make sure iteration order is consistent
			this.sum=new LinkedHashSet<ReductionTarget>(targets);
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
						skip = new HashSet<ReductionTarget>();
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
						addKey(val, value_name);
						indexes++;
					}else{
						if( val instanceof SQLExpression){
							if( Number.class.isAssignableFrom(val.getTarget())){
								SQLExpression<? extends Number> e = (SQLExpression<? extends Number>) val;


								//log.debug("reduction on number property "+t+" maps to "+e);
								switch(target.getReduction()){
								case SUM: addSum(e,value_name);break;
								case MIN: addMin(e, value_name);break;
								case MAX: addMax(e, value_name);break;
								case AVG: addAverage(e, value_name);break;
								case SELECT:addClause(val, value_name);break; 
								default: throw new IllegalReductionException("Bad number reduction");
								}
							}else if( Date.class.isAssignableFrom(val.getTarget()) ){
								SQLExpression<? extends Date> e = (SQLExpression<? extends Date>) val;


								//log.debug("reduction on date property "+t+" maps to "+e);
								switch(target.getReduction()){
								case MIN: addMinDate(e, value_name);break;
								case MAX: addMaxDate(e, value_name);break;
								case SELECT:addClause(val, value_name);break; 
								default: throw new IllegalReductionException("Bad date reduction");
								}

							}else{
								if( target.getReduction() == Reduction.SELECT){
									addClause(val, value_name);
								}else{
									throw new IllegalReductionException("Unsupported data type for reduction");
								}
							}
						}else{
							if( target.getReduction() == Reduction.SELECT){
								addClause(val, value_name);
							}else{
								throw new CannotUseSQLException("Not an SQL Expression");
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
		protected ExpressionTuple makeKey(ResultSet rs) throws DataException {
			Map<PropExpression,Object> map = new HashMap<PropExpression, Object>();
			int pos=0;
			for(ReductionTarget target : sum){
		
					if( target.getReduction() == Reduction.INDEX){

						PropExpression tag =  target.getExpression();
						map.put(tag, getTargetObject(pos, rs));
					}
					//TODO a multi field SQLValue SELECT will fail
					pos++;

			}
			
			return new ExpressionTuple(map);
			
		}

		@Override
		protected ReductionMapResult makeResult(ResultSet rs) throws DataException {
			ReductionMapResult map = new ReductionMapResult();
			int pos=0;
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
					//TODO multi field SQLValue SELECt will fail
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