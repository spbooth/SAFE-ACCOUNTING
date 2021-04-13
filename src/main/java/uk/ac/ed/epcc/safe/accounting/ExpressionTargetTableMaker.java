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
package uk.ac.ed.epcc.safe.accounting;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.LabellerTransform;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.content.Transform;
import uk.ac.ed.epcc.webapp.limits.LimitService;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** Class to build a table of expressions values with one row per record.
 * 
 * @author spb
 * @param <E> ExpresionTarget
 * @param <F> ExpressionTargetGenerator
 *
 */


public class ExpressionTargetTableMaker<E,F extends ExpressionTargetGenerator<E>> extends AbstractContexed{
	
	private F up;

	private List<ColName> col_names;
	private int max_data_points=-1;
	private int skip_data_points=0;
	private PropExpression warning=null;
	private final int check_every;
	public ExpressionTargetTableMaker(AppContext c,F up){
		super(c);
		this.up=up;
		col_names=new LinkedList<>();
		
		check_every=c.getIntegerParameter("expression_target_table_maker.check_every", 500);
	}
	public ExpressionTargetTableMaker(AppContext c,F up, List<ColName> props){
	    this(c,up);
		for(ColName col : props){
			addColumn(col);
		}
	}
	public final void addColumn(ColName col) {
		Logger log = getLogger();
		PropExpression<?> t = col.getTag();
		if( up.compatible(t)){
			col_names.add(col);
		}else{
			log.debug("Property "+t.toString()+" not supported");
		}
	}
	public final int setMaxDataPoints(int value){
		int old = max_data_points;
		max_data_points=value;
		return old;
	}
	public final int setSkipDataPoints(int value){
		int old = skip_data_points;
		skip_data_points=value;
		return old;
	}
	public final void setWarningExpression(PropExpression exp){
		warning=exp.copy();
	}
	protected Object makeKey(E t){
		if( up instanceof IndexedProducer){
			return ((IndexedProducer)up).makeReference((Indexed)t);
		}
		return t;
	}
	
	
	@SuppressWarnings("unchecked")
	public  final Table<String,Object> makeTable( RecordSelector sel) throws Exception{
	   Table res = new Table();
	  
	   try(CloseableIterator<E> it = ( max_data_points >= 0 )?
			   up.getIterator(sel, skip_data_points, max_data_points > 0 ?(skip_data_points+max_data_points):-1)
			   : up.getIterator(sel)){

		   int count=0;
		   while(it.hasNext()){
			   E record = it.next();
			   Object key = makeKey(record);
			   ExpressionTargetContainer et = up.getExpressionTarget(record);
			   for(ColName c : col_names){
				   String lab = c.getName();
				   PropExpression t = c.getTag();
				   try{
					   Object val = et.evaluateExpression(t);
					   if( val != null){
						   PropExpression dyn = c.getNameExpression();
						   if( dyn == null) {
							   res.put(lab, key,val );
						   }else {
							  
							   Object dlab = et.evaluateExpression(dyn);
							   if( dlab != null ) {
								   res.put(dlab, key, val);
								   if( lab != null && ! lab.isEmpty()) {
									   res.addToGroup(lab, dlab);
								   }
							   }
						   }
					   }
				   }catch(InvalidExpressionException e){
					   getLogger().debug("Skipping invalid property "+lab+"->"+t.toString(), e);
				   }catch( Exception e2) {
					   getLogger().error("Error in expression evaluation "+lab+"->"+t.toString(), e2);
				   }
			   }
			   if( warning != null ){
				   try{
					   boolean set =false;
					   Object val = et.evaluateExpression(warning);
					   if( val != null){
						   if( val instanceof Boolean){
							   set =((Boolean)val).booleanValue();
						   }
						   if( val instanceof String){
							   set = val.toString().trim().length() > 0;
						   }
						   if( val instanceof Number){
							   set = ((Number)val).intValue() > 0;
						   }
					   }
					   res.setWarning(key, set);
				   }catch(Exception t){
					  getLogger().error("Error evaluating warning",t);
				   }
			   }
			   if( key != et) {
				   et.release();
			   }
			   count++;
			   if( check_every > 0 && count%check_every == 0) {
				   LimitService limit = getContext().getService(LimitService.class);
				   if( limit != null) {
					   limit.checkLimit();
				   }
			   }
		   }
	   }
	   // Now set formats
	   for(ColName c : col_names){
		   String lab = c.getName();
		   PropExpression t = c.getTag();
		   if( Number.class.isAssignableFrom(t.getTarget())){
			   res.setColFormat(lab, new Transform() {
				
				public Object convert(Object old) {
					if( old == null ){
						return Double.valueOf(0.0);
					}
					return old;
				}
			});
		   }
		   if( Date.class.isAssignableFrom(t.getTarget())){
			   res.setColFormat(lab, new Transform(){
				   DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,getContext().getService(SessionService.class).getLocale());
				public Object convert(Object old) {
					if( old instanceof Date){
						return df.format((Date) old);
					}
					return old;
				}
				   
			   });
		   }
		   if( t instanceof FormatProvider){
			   res.setColFormat(lab,new LabellerTransform(getContext(),((FormatProvider)t).getLabeller()));
		   }
		   Transform trans = c.getTransform();
		   if( trans != null ){
			   res.setColFormat(lab, trans);
		   }
	   }
	   return res;
	}
    
}