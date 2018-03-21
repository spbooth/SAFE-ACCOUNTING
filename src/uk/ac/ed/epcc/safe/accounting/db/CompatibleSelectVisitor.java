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
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;
import uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.NullSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OrRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OrderClause;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.ReductionSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RelationClause;
import uk.ac.ed.epcc.safe.accounting.selector.RelationshipClause;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.safe.accounting.selector.SelectorVisitor;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.logging.Logger;

/** A SelectVisitor that checks if a given RecordSelector is compatible with
 * a UsageProducer
 * 
 * @author spb
 *
 */


public class CompatibleSelectVisitor implements SelectorVisitor<Boolean>{

	private final ExpressionFilterTarget<?> eft;
	private boolean require_sql;
	private Logger log;
	public CompatibleSelectVisitor(Logger log,ExpressionFilterTarget<?> up,boolean require_sql){
		this.log=log;
		this.eft=up;
		this.require_sql=require_sql;
	}
	public CompatibleSelectVisitor(ExpressionFilterTarget<?> up,boolean require_sql){
		this(null,up,require_sql);
	}
	public Boolean visitAndRecordSelector(AndRecordSelector a) throws Exception {
		for(RecordSelector s: a){
			if( ! s.visit(this)){
				return false;
			}
		}
		return true;
	}

	public <I> Boolean visitClause(SelectClause<I> c) throws Exception {
		if( c == null || c.tag==null){
			return false;
		}
		//expensive but only way to ensure this accurately reflects all the
		//behaviour of getFilter
		try{
			BaseFilter<?> fil = eft.getFilter(c.tag, c.match, c.data);
			if( require_sql ){
				try{
					FilterConverter.convert(fil);
				}catch(NoSQLFilterException e){
					if( log != null ){
						log.debug("cannot make SQL filter"+e.getMessage());
					}
					return false;
				}
			}
		}catch(CannotFilterException e){
			if( log != null ){
				log.debug("cannot make filter"+e.getMessage());
			}
			return false;
		}
		return true;
	}

	public Boolean visitOrRecordSelector(OrRecordSelector o) throws Exception {
		for(RecordSelector s: o){
			if( s.visit(this)){
				return true;
			}
		}
		return false;
	}
	public <I> Boolean visitNullSelector(NullSelector<I> n) throws Exception {
		if(n == null){
			return false;
		}
		try{
			BaseFilter<?> fil = eft.getNullFilter(n.expr, n.is_null);
			if( require_sql ){
				try{
					FilterConverter.convert(fil);
				}catch(NoSQLFilterException e){
					if( log != null ){
						log.debug("cannot make null SQL filter"+e.getMessage());
					}
					return false;
				}
			}
		}catch(CannotFilterException e){
			if( log != null ){
				log.debug("cannot make null filter"+e.getMessage());
			}
			return false;
		}
		return true;
	}
	
	public <I> Boolean visitRelationClause(RelationClause<I> c)
			throws Exception {
		if( c == null ){
			return false;
		}
		try{
			BaseFilter<?> fil = eft.getRelationFilter(c.left, c.match, c.right);
			if( require_sql){
				try{
					FilterConverter.convert(fil);
				}catch(NoSQLFilterException e){
					if( log != null ){
						log.debug("cannot make SQL relation filter"+e.getMessage());
					}
					return false;
				}
			}
		}catch(CannotFilterException e){
			if( log != null ){
				log.debug("cannot make relation filter"+e.getMessage());
			}
			return false;
		}
		return true;
	}
	public Boolean visitPeriodOverlapRecordSelector(
			PeriodOverlapRecordSelector o) throws Exception {
		if( o == null ){
			return false;
		}
		try{
			BaseFilter<?> fil = eft.getPeriodFilter(o.getPeriod(), o.getStart(), o.getEnd(),o.getType(),o.getCutoff());
			if( require_sql){
				try{
					FilterConverter.convert(fil);
				}catch(NoSQLFilterException e){
					if( log != null ){
						log.debug("cannot make SQL period filter"+e.getMessage());
					}
					return false;
				}
			}
		
		}catch(CannotFilterException e){
			if( log != null ){
				log.debug("cannot make period filter"+e.getMessage());
			}
			return false;
		}
		return true;
	}
	public <I> Boolean visitOrderClause(OrderClause<I> o) throws Exception {
		try{
			eft.getOrderFilter(o.getDescending(), o.getExpr());
		}catch(CannotFilterException e){
			return false;
		}
		return true;
	}
	public Boolean visitReductionSelector(ReductionSelector r) throws Exception {
		int seen=0;
		int skip=0;
		for(ReductionTarget t : r){
			if( t.getReduction() == Reduction.INDEX){
				try{
					BaseFilter<?> fil = eft.getNullFilter(t.getExpression(), false);
					if( require_sql ){
						try{
							FilterConverter.convert(fil);
						}catch(NoSQLFilterException e){
							if( log != null ){
								log.debug("cannot make null SQL filter"+e.getMessage());
							}
							return false;
						}
					}
				}catch(CannotFilterException e){
					if( log != null ){
						log.debug("cannot make null filter"+e.getMessage());
					}
					return false;
				}
			}else{
				seen++;
				try{
					BaseFilter<?> fil = eft.getNullFilter(t.getExpression(), false);
					if( require_sql ){
						try{
							FilterConverter.convert(fil);
						}catch(NoSQLFilterException e){
							if( log != null ){
								log.debug("cannot make null SQL filter"+e.getMessage());
							}
							skip++;
						}
					}
				}catch(CannotFilterException e){
					if( log != null ){
						log.debug("cannot make null filter"+e.getMessage());
					}
					skip++;
				}
			}
		}
		if( seen > 0){
			return skip < seen;
		}
		return true;
	}
	@Override
	public Boolean visitRelationshipClause(RelationshipClause r) throws Exception {
		try{
			eft.getRelationshipFilter(r.getRelationship());
			return true;
		}catch(CannotFilterException e){
			if( log != null ){
				log.debug("cannot make relationship filter"+e.getMessage());
			}
			return false;
		}
	}

}