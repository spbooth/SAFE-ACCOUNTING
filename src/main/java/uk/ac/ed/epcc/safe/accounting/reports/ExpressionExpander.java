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
package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DefaultFormatter;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** Class for performing prop-expression macro expansion on strings. 
 * Macros are of the form <b>$[format]{expr}</b> or <b>${expr}</b>
 * 
 * If a format is specified it is taken as a class tag for a {@link ValueParser}
 * If no format is specified a default formatter is chosen based on the type of the parameter.
 * @author spb
 *
 */


public class ExpressionExpander extends AbstractContexed{
  
  private final ValueParserPolicy policy;
  private ExpressionTarget container=null;
  private Parser parser=null;
  public ExpressionExpander(AppContext c,ValueParserPolicy policy){
	  super(c);
	  this.policy=policy;
  }
  
  public void setExpressionTarget(ExpressionTarget cont){
	  container=cont;
	  parser = cont.getParser();
  }
  
  public ExpressionTarget getExpressionTarget(){
	  return container;
  }
  /** Get the ValueFormatter
   * 
  
   * @param tag
   * @param format
   * @return
   */
private  <T> ValueParser<T> getFormatter(Class<T> tag,String format){
	  

	  ValueParser<T> res = policy.getValueParser(tag,format);
	  if( res == null ){
		  res = policy.getValueParser(tag);
		  if( res == null ){
			  return new DefaultFormatter<>(tag);
		  }
	  }
	  assert(res==null || res.getType().isAssignableFrom(tag));
	  return res;
  }
  
  private static final Pattern var_pattern = Pattern.compile("\\$(?:\\[(\\w+)\\])?\\{([^\\}]+)\\}");
  @SuppressWarnings("unchecked")
public <T> String expand(String input){
	  StringBuffer result = new StringBuffer();
	  Matcher m = var_pattern.matcher(input);
	  Logger log = conn.getService(LoggerService.class).getLogger(getClass());
	  while(m.find()){
		  String text="";
		  String format = m.group(1);
		  String name = m.group(2);
		  log.debug("found a macro "+name+" "+format);
		  try{
		  PropExpression<T> expr = parser.parse(name);
		  
			  if( expr != null ){
				  ValueParser<? super T> fmt = getFormatter(expr.getTarget(), format);
				  T val = container.evaluateExpression(expr);
				  if( val != null ){
					  log.debug("expr="+expr.toString()+" value="+val.toString()+" format="+fmt.getClass().getCanonicalName());
					  if( fmt.getType().isAssignableFrom(val.getClass())){
						  text = fmt.format(val);
					  }else{
						  getLogger().error("Bad formatter selected for "+expr.toString()+":"+expr.getTarget().getCanonicalName()+" passed "+val.getClass().getCanonicalName()+" "+val.toString());
					  }
				  }else{
					  log.warn("Property "+expr.toString()+" null in MacroExpander");
				  }
			  }else{
				  getLogger().error("bad expression "+name);
			  }
		  }catch(Exception t){
			  getLogger().error("Cannot parse expression",t);
		  }
		  text = text.replace("\\", "\\\\");
		  text = text.replace("$", "\\$");
		  m.appendReplacement(result, text);
	  }
	  m.appendTail(result);
	  return result.toString();
		
  }
  @SuppressWarnings("unchecked")
public <T> boolean isDefined(String name){
	  try{
		  PropExpression<T> expr = parser.parse(name);
		  if( expr == null ){
			  return false;
		  }
		  T val = container.evaluateExpression(expr);
		  if( val == null){
			  return false;
		  }
		  if( val instanceof IndexedReference && ((IndexedReference)val).isNull()){
			  return false;
		  }
		  return true;
	  }catch(Exception t){
		  
	  }
	  return false;
  }
  public PropExpression parse(String name){
	  try {
		return parser.parse(name);
	} catch (Exception e) {
		getLogger().error("Error parsing expression",e);
		return null;
	}
  }

public <T> T evaluate(PropExpression<T> expr){
	  try{
		  if( expr == null ){
			  return null;
		  }
		  T val = container.evaluateExpression(expr);
		  return val;
	  }catch(Exception t){
		  getLogger().error( "Error expanding expression",t);
	  }
	  return null;
  }

	
}