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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.LabellerTransform;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.content.Transform;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
/** Class to build a table of expressions values with one row per record.
 * 
 * @author spb
 * @param <E> ExpresionTarget
 * @param <F> ExpressionTargetGenerator
 *
 */


public class ExpressionTargetTableMaker<E extends ExpressionTarget,F extends ExpressionTargetGenerator<E>>{
	private AppContext c;
	private F up;
	private Map<String,PropExpression> props;
	private Map<String,Transform> transforms;
	private List<String> labels;
	private int max_data_points=-1;
	private int skip_data_points=0;
	private PropExpression warning=null;
	public ExpressionTargetTableMaker(AppContext c,F up){
		this.c=c;
		this.up=up;
		labels=new LinkedList<String>();
		this.props=new HashMap<String,PropExpression>();
		this.transforms=new HashMap<String, Transform>();
	}
	public ExpressionTargetTableMaker(AppContext c,F up, List<ColName> props){
	    this(c,up);
		for(ColName col : props){
			addColumn(col);
		}
	}
	public final void addColumn(ColName col) {
		Logger log = c.getService(LoggerService.class).getLogger(getClass());
		PropExpression<?> t = col.getTag();
		if( up.compatible(t)){
			labels.add(col.getName());
			this.props.put(col.getName(),t);
			Transform l = col.getTransform();
			if( l != null){
				this.transforms.put(col.getName(), l);
			}
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
	  
	 
	   Iterator<E> it;
	   if( max_data_points >= 0 ){
		   it = up.getIterator(sel, skip_data_points, max_data_points > 0 ?(skip_data_points+max_data_points):-1);
	   }else{
		   it= up.getIterator(sel);
	   }
	   while(it.hasNext()){
		   E record = it.next();
		   Object key = makeKey(record);
		   for(String lab : labels){
			   PropExpression t = props.get(lab);
			   try{
			   Object val = record.evaluateExpression(t);
			   if( val != null){
				res.put(lab, key,val );
			   }
			   }catch(InvalidPropertyException e){
				   
			   }
		   }
		   if( warning != null ){
			   try{
				   boolean set =false;
				   Object val = record.evaluateExpression(warning);
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
			   }catch(Throwable t){
				   c.getService(LoggerService.class).getLogger(getClass()).error("Error evaluating warning",t);
			   }
		   }
	   }
	   for(String lab : labels){
		   PropExpression t = props.get(lab);
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
				   DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
				public Object convert(Object old) {
					if( old instanceof Date){
						return df.format((Date) old);
					}
					return old;
				}
				   
			   });
		   }
		   if( t instanceof FormatProvider){
			   res.setColFormat(lab,new LabellerTransform(c,((FormatProvider)t).getLabeller()));
		   }
		   Transform trans = transforms.get(lab);
		   if( trans != null ){
			   res.setColFormat(lab, trans);
		   }
	   }
	   return res;
	}
    
}