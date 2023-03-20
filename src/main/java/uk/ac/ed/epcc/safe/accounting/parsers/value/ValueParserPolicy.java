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
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.expr.*;
import uk.ac.ed.epcc.safe.accounting.properties.MethodPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** A {@link PropExpressionVisitor} to create a {@link ValueParser} appropriate to
 * a {@link PropExpression}. 
 * This class defaults to using the {@link ValueParserService} 
 * but can be sub-classed to customise the behaviour. 
 * 
 * This class also caches the {@link ValueParser}s by {@link PropertyTag} so a single visitor should be used for
 * an entire parse pass.
 * @author spb
 *
 */


public class ValueParserPolicy implements
		PropExpressionVisitor<ValueParser> {


	private ValueParserService serv;
	private boolean use_xml=false;
	private String format;
	private Map<PropertyTag,ValueParser> cache=new HashMap<>();
	private Map<Class,ValueParser> defaultParsers= new HashMap<>();
	
	public ValueParserPolicy(AppContext conn){
		serv = conn.getService(ValueParserService.class);
		 
	}
	public void setFormat(String format){
		this.format=format;
	}
	public boolean setXML(boolean xml){
		boolean old = this.use_xml;
		this.use_xml=xml;
		return old;
	}
	public final <T> ValueParser<T> getValueParser(Class<? extends T> clazz)
	{
		if( format != null && ! format.isEmpty()){
			return getValueParser(clazz, format);
		}
		return getDefaultValueParser(clazz);
	}
	@SuppressWarnings("unchecked")
	public final <T> ValueParser<T> getDefaultValueParser(Class<? extends T> clazz){
		ValueParser res = defaultParsers.get(clazz);
		if( res == null ){
			res = makeValueParser(clazz);
			if( res != null ){
				defaultParsers.put(clazz,res);
			}
		}
		return res;
	}
	@SuppressWarnings("unchecked")
	public final <T> ValueParser<T> getValueParser(Class<? extends T> clazz,String tag){
		if( tag != null && ! tag.isEmpty()) {
			ValueParser res = serv.getValueParser(tag);
			if( res != null && clazz.isAssignableFrom(res.getType())){
				return res;
			}
		}
		return getDefaultValueParser(clazz);
	}
	/** generate a default parser based on the class of the object.
	 * This is an extension point that can be overridden in sub-classes.
	 * @param clazz
	 * @return ValueParser
	 */
	
	@SuppressWarnings("unchecked")
	public ValueParser makeValueParser(Class clazz){
		
		  if( clazz == String.class){
			  return new StringParser();
		  }else if( clazz == Number.class){
			  return new NumberParser();
		  }else if( clazz == Integer.class){
			  return new IntegerParser();
		  }else if( clazz == Long.class){
			  return new LongParser();
		  }else if( clazz == Float.class){
			  return new FloatParser();
		  }else if ( clazz == Double.class){
			  return new DoubleParser();
		  }else if( clazz == Boolean.class){
			  return new BooleanParser();
		  }else if ( clazz == Date.class){
			  if( use_xml){
				  return new XMLDateTimeParser();
			  }else{
				  return new DateTimeParser();
			  }
		  }else if ( clazz == Duration.class){
			  if( use_xml ){
				  return new XMLDurationParser();
			  }else{
				  return new SimpleDurationParser();
			  }
		  }else if( clazz == IndexedReference.class){
			  return new IndexedReferenceValueParser(serv.getContext());
		  }
		  return new DefaultFormatter(clazz);
		
	}
	
	public ValueParser visitStringPropExpression(
			StringPropExpression<?> stringExpression) throws Exception {
		return getValueParser(stringExpression.getTarget());
	}
	public ValueParser visitIntPropExpression(
			IntPropExpression<?> intExpression) throws Exception {
		return getValueParser(intExpression.getTarget());
	}
	public ValueParser visitFloorPropExpression(
			FloorPropExpression<?> intExpression) throws Exception {
		return getValueParser(intExpression.getTarget());
	}
	public ValueParser visitCeilPropExpression(
			CeilPropExpression<?> intExpression) throws Exception {
		return getValueParser(intExpression.getTarget());
	}
	public ValueParser visitLongCastPropExpression(
			LongCastPropExpression<?> intExpression) throws Exception {
		return getValueParser(intExpression.getTarget());
	}
	public ValueParser visitDoubleCastPropExpression(
			DoubleCastPropExpression<?> expression) throws Exception {
		return getValueParser(expression.getTarget());
	}
	public ValueParser visitDurationCastPropExpression(
			DurationCastPropExpression<?> expression) throws Exception {
		return getValueParser(expression.getTarget());
	}
	public ValueParser visitConstPropExpression(
			ConstPropExpression<?> constExpression) throws Exception {
		return getValueParser(constExpression.getTarget());
	}


	public ValueParser visitBinaryPropExpression(
			BinaryPropExpression binaryPropExpression) throws Exception {
		return getValueParser(binaryPropExpression.getTarget());
	}

	public ValueParser visitMilliSecondDatePropExpression(
			MilliSecondDatePropExpression milliSecondDate) throws Exception {
		return getValueParser(milliSecondDate.getTarget());
	}

	public ValueParser visitNamePropExpression(
			NamePropExpression namePropExpression) throws Exception {
		return getValueParser(namePropExpression.getTarget());
	}
	public <T extends DataObject> ValueParser visitDoubleDeRefExpression(
			DoubleDeRefExpression<T, ?> deRefExpression) throws Exception {
		
		return deRefExpression.getExpression().accept(this);
	}
	public <T extends DataObject> ValueParser visitDeRefExpression(
			DeRefExpression<T, ?> deRefExpression) throws Exception {
		
		return deRefExpression.getExpression().accept(this);
	}

	public ValueParser visitPropertyTag(PropertyTag<?> tag) throws Exception {
		String name = tag.getFullName();
		ValueParser p = cache.get(tag);
	
		if( p != null ){
			return p;
		}
		p = serv.getValueParser(name);
		if( p == null && tag instanceof ValueParserProvider){
			return ((ValueParserProvider)tag).getValueParser(serv.getContext());
		}
		if( p == null ){
			p = getValueParser(tag.getTarget());
		}
		if( p != null ){
			cache.put(tag, p);
		}
		return p;
	}

	public ValueParser visitSelectPropExpression(SelectPropExpression<?> sel)
			throws Exception {
		return getValueParser(sel.getTarget());
	}

	public ValueParser visitDurationPropExpression(DurationPropExpression sel)
			throws Exception {
		return getValueParser(sel.getTarget());
	}
	public <T, D> ValueParser visitTypeConverterPropExpression(
			TypeConverterPropExpression<T, D> sel) throws Exception {
		return getValueParser(sel.getTarget());
	}
	public <T,R> ValueParser visitLabelPropExpression(LabelPropExpression<T,R> expr)
			throws Exception {
		return getValueParser(expr.getTarget());
	}
	public ValueParser visitDurationSecondPropExpression(
			DurationSecondsPropExpression d) throws Exception {
		return getValueParser(d.getTarget());
	}
	public <T> ValueParser visitCasePropExpression(CasePropExpression<T> expr)
			throws Exception {
		return getValueParser(expr.getTarget());
	}
	public ValueParser visitConvetMillisecondToDateExpression(
			ConvertMillisecondToDatePropExpression expr) throws Exception {
		
		return getValueParser(expr.getTarget());
	}
	@Override
	public <C extends Comparable> ValueParser visitCompareExpression(
			ComparePropExpression<C> expr) throws Exception {
		return getValueParser(expr.getTarget());
	}
	@Override
	public <I extends Indexed> ValueParser visitConstReferenceExpression(ConstReferenceExpression<I> expr)
			throws Exception {
		return getValueParser(expr.getTarget());
	}
	@Override
	public ValueParser visitLocatePropExpression(LocatePropExpression expr) throws Exception {
		return getValueParser(expr.getTarget());
	}
	@Override
	public ValueParser visitArrayFuncPropExpression(ArrayFuncPropExpression expr) throws Exception {
		
		return getValueParser(expr.getTarget());
	}
	@Override
	public ValueParser visitMethodPropExpression(MethodPropExpression<?> method) throws Exception {
		return getValueParser(method.getTarget());
	}

}