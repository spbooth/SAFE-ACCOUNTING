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
import uk.ac.ed.epcc.safe.accounting.parsers.value.DefaultFormatter;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/** Class for performing property macro expansion on strings. 
 * Macros are of the form <b>$[format]{name}</b> or <b>${name}</b>
 * 
 * If a format is specified it is taken as a class tag for a {@link ValueParser}
 * If no format is specified a default formatter is chosen based on the type of the parameter.
 * 
 * Might be better to use {@link ExpressionExpander} if the target is an {@link ExpressionTarget}
 * @author spb
 * @see ExpressionExpander
 *
 */


public class MacroExpander extends AbstractContexed{
 
  private final ValueParserPolicy policy;
  private PropertyContainer container=null;
  private PropertyFinder finder=null;
  public MacroExpander(AppContext c,ValueParserPolicy policy){
	  super(c);
	  this.policy=policy;
  }
  public void setPropertyContainer(PropertyContainer cont){
	  container=cont;
  }
  public void setPropertyFinder(PropertyFinder f){
	  this.finder=f;
  }
  public PropertyFinder getFinder(){
	  return finder;
  }
  public PropertyContainer getContainer(){
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
		  if( finder != null && container != null){
			  PropertyTag<T> t = (PropertyTag<T>) finder.find(name);
			  if( t != null ){
				  ValueParser<? super T> fmt = getFormatter(t.getTarget(), format);
				  T val = container.getProperty(t, null);
				  if( val != null ){
					  log.debug("property="+t.getFullName()+" value="+val.toString()+" format="+fmt.getClass().getCanonicalName());
					  if( t.allow(val) && fmt.getType().isAssignableFrom(val.getClass())){
						  text = fmt.format(val);
					  }else{
						  getLogger().error("Bad formatter selected for "+t.getFullName()+":"+t.getTarget().getCanonicalName()+" passed "+val.getClass().getCanonicalName()+" "+val.toString());
					  }
				  }else{
					  log.warn("Property "+t.getFullName()+" null in MacroExpander");
				  }
			  }else{
				  getLogger().error("non existent property "+name);
			  }
		  }
		  m.appendReplacement(result, text);
	  }
	  m.appendTail(result);
	  return result.toString();
		
  }
}