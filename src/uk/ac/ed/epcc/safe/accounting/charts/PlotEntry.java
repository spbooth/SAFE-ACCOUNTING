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
package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ParseException;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.config.FilteredProperties;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/** Class defining the quantity to plot and how it should be mapped to time periods.
 * 
 * If both time properties are set then the value should be overlapped 
 * 
 * @author spb
 *
 */


public class PlotEntry {
	  private final String name;  // short name for plot entry 
	  private final String description; // text to be presented to user
	  private final PropExpression<? extends Number> prop_tag;  // property to plot
	  private final PlotEntry norm;
	  private final PropertyTag<Date> start_prop;
	  private final PropertyTag<Date> end_prop;
	  private long cutoff=0L; // max job length to consider
	  private double scale=1.0;
	  private double time_scale=1.0;
	  private boolean rate_scale=true;
	  private String label=null;
	  private String time_unit=null;
	 
	 
	  private Reduction red=Reduction.SUM;

	  public PlotEntry(PropExpression<? extends Number> prop, PlotEntry norm,PropertyTag<Date> start, PropertyTag<Date> end,String name,String desc){
		  this.name=name;
		  this.description=desc;
		  this.prop_tag=prop;
		  this.norm=norm;
		  this.start_prop=start;
		  this.end_prop=end;
	  }
	  public PlotEntry(PropExpression<? extends Number> prop, PropertyTag<Date> start, PropertyTag<Date> end,String name,String desc){
		  this(prop,null,start,end,name,desc);
	  }
			
	  public PlotEntry(PropExpression<? extends Number> prop, PropertyTag<Date> target,String name,String desc){
		  this(prop,null,target,name,desc);
	  }
	  public String getName(){
		  return name;
	  }
	  public String getDescription(){
		  return description;
	  }
	  /** Property to plot
	   * 
	   * @return PropertyTag
	   */
	  public PropExpression<? extends Number> getPlotProperty(){
		  return prop_tag;
	  }
	  
	  /** get a PlotEntry corresponding to the normalisation expression.
	   * 
	   * @return PlotEntry
	   */
	  public PlotEntry getScaleEntry(){
		   return norm; 
	  }
	  /** Get PropertyTag<Date> defining start of active period. This can be null indicating that
	   * overlap scaling should not be used.
	   * 
	   * @return PropertyTag<Date>
	   */
	  public PropertyTag<Date> getStartProperty(){
		  return start_prop;
	  }
	  /** Get PropertyTag<Date> defining the end of the active period for this record.
	   * 
	   * @return PropertyTag<Date>
	   */
	  public PropertyTag<Date> getEndProperty(){
		  return end_prop;
	  }
	  /** Get a cutoff length in milliseconds. When using overlap scaling the code can optimise based
	   * on the assumption that no record has an active period longer than this.
	   * 
	   * @return long milliseconds.
	   */
	  public long getCutoff(){
		  return cutoff;
	  }
	  public void setCutoff(long cutoff){
		  this.cutoff=cutoff;
	  }
	  /** Get the Quantity axis label to use for this plot (if any).
	   * 
	   * 
	   * @return String
	   */
	  public String getLabel(){
		  return label;
	  }
	  public void setLabel(String label){
		  this.label=label;
	  }
	  /** Get the multiplicative scale factor to convert this plot into the desired units.
	   * In practice it is often easier to use a PropExpression for this
	   * @return double
	   */
	public double getScale() {
		return scale;
	}
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public Reduction getReduction(){
		return red;
	}
	public void setReduction(Reduction op){
		this.red=op;
		if( op == Reduction.AVG){
			setRateScale(false);
		}
	}

	/** Get the size of the desired time units (in milliseconds).
	 * only applies is RateScale is enabled.
	 * @return double number of milliseconds in time unit
	 */
	public double getTimeScale() {
		if( ! isRateScale()){
			return 1.0;
		}
		return time_scale;
	}
	public void setTimeScale(double time_scale) {
		this.time_scale = time_scale;
	}
	/** Should TimeChart data be scaled according to size of plot periods.
	 * This converts the quantity to a rate.
	 * 
	 * @return boolean
	 */
	public boolean isRateScale() {
		if( start_prop == null ){
			return false;
		}
		if( red != Reduction.SUM){
			return false;
		}
		return rate_scale;
	}
	public void setRateScale(boolean rate_scale) {
		
		this.rate_scale = rate_scale;
	}
	/** Get name of time unit to use in RateScale plots.
	 * @return String or null
	 */
	public String getTimeUnit() {
		if( ! isRateScale() ){
			return null;
		}
		return time_unit;
	}
	public void setTimeUnit(String time_unit) {
		this.time_unit = time_unit;
	}
	

    public boolean compatible(UsageProducer<?> ap){
    	
    	boolean result=true;
    	result = result && (prop_tag == null || ap.compatible(prop_tag));
    	
    	result = result && (start_prop == null || ap.hasProperty(start_prop));
    	
        result = result && ( end_prop == null  || ap.hasProperty(end_prop));
       
    	return result;
    }
    /** Generate a HTML selector for the possible PlotEntries
     * 
     * @param plot_type
     * @param list  Vector of MapperEntry
     * @return String HTML fragment selecting PlotEntries
     */
    @Deprecated
   public static String MapSelector(String plot_type, Set<PlotEntry> list) {
       
       String result="";
       
       boolean first=true;
       for(Iterator it=list.iterator();it.hasNext();){
           PlotEntry me = (PlotEntry) it.next();
           result += "<tr><td><input type=\"radio\" name=\"quant\" value=\"";
           result += me.getDescription();
           result += "\"";
           if( (plot_type==null && first) ||( plot_type != null && plot_type.equals(me.getDescription()))){
               result += " checked ";
           }    
           result += "> "+me.getDescription()+" </td></tr>";
           first=false;
       }
       return result;
   }
   public static String getMapType(String plot_type, Set<PlotEntry> list) {
	 
       if( plot_type == null && ! list.isEmpty() ){
           plot_type=list.iterator().next().getDescription();
       }
       return plot_type;
   }
   /** parse the HTML request for the possible mappers.
    * 
    * @param plot_type
 * @param list Vector of MapperEntry
    * @return MapperEntry
    */
   @Deprecated
   public static PlotEntry parseMapSelector(String plot_type, Set<PlotEntry> list) {
       
       for(Iterator it=list.iterator();it.hasNext();){
           PlotEntry me = (PlotEntry) it.next();
           if( plot_type == null || plot_type.equals(me.getDescription())){
               return me;
           }

       }
       return null;
       
   }
   /** Get the set of PlotEntrys defined by the config properties
 * @param finder 
    * 
    * @param c  AppContext
    * @return Set of PlotEntry
    */
public static Set<PlotEntry> getPlotSet(PropertyFinder finder, AppContext c) {
	return getPlotSet(finder,c,null);
}
public static Set<PlotEntry> getPlotSet(PropertyFinder finder, AppContext c,String tag) {
	    
	       FilteredProperties prop = new FilteredProperties(c.getService(ConfigService.class).getServiceProperties(), "PlotEntry",tag);
		   Set<PlotEntry> set = new LinkedHashSet<PlotEntry>();
		   Logger log = c.getService(LoggerService.class).getLogger(PlotEntry.class);
		   String list = prop.getProperty("list");
		   log.debug("list is"+list);
		   if( list != null && list.trim().length() != 0){
			   for(String name : list.split(",")){
				   log.debug("Consider "+name);
				   ErrorSet errors = new ErrorSet();
					   PlotEntry entry = getConfigPlotEntry(c,errors,prop, finder, name,null,null);
					   if( entry != null){
						   set.add(entry);
					   }
				   if( errors.hasError()){
					   errors.report(log);
					  
				   }
			   }
		   }
		  
		   return set;
   }
  
   public static PlotEntry getPlotEntry(AppContext c,ErrorSet errors,PropertyFinder finder,String name,String start,String end) throws Exception{
	   try{
		   return c.getService(ChartService.class).getPlotEntry(errors,finder, name, start, end);
	   }catch(Exception e){
		   c.getService(LoggerService.class).getLogger(PlotEntry.class).error("Error making PlotEntry name=<"+name+">",e);
		   throw e;
	   }
   }
   /** Get a PlotEntry from properties
    * 
    * @param conn AppContext
    * @param prop FilteredProperties
    * @param finder PropertyFinder
    * @param name 
    * @param start default start prop name if not in prop
    * @param end default end prop name if not in prop
    * @return PlotEntry
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   public static PlotEntry getConfigPlotEntry(AppContext conn,ErrorSet errors,FilteredProperties prop,PropertyFinder finder,String name,String start,String end) {
	   //Note that although this code is using the FilteredProperties the
	   // ChartExtension currently does NOT so The tag/mode should only be used to set the
	   // list
	   if( name == null || finder == null ){
		   return null;
	   }
	   Logger log = conn.getService(LoggerService.class).getLogger(PlotEntry.class);
	 
	 
	   // Name may be the user presented text or an expression so remove spaces to make the tag.
	   name=name.trim();
	   String tag =name.trim().replaceAll("\\s", "_")+".";
	   // if we have an explicit plot expression parse it.
	   Parser parser = new Parser(conn,finder);

	   // long form description defaults to the name (with spaces)
	   String desc=prop.getProperty(tag+"description",name);
	   // get the plot expression string (default to name again)
	   String plot_tag=prop.getProperty(tag+"plot",name);
	   PropExpression plot =null;
	   if( plot_tag != null && plot_tag.trim().length() > 0){
		   try{
			  	plot = parser.parse(plot_tag);
		   }catch(Exception e){
			   errors.add("Error parsing plot expression", plot_tag,e);
			   log.warn("Error parsing plot tag", e);
		   }
	   }
	   PlotEntry norm=null;
	   String norm_tag =prop.getProperty(tag+"norm");
	   if( norm_tag != null && norm_tag.trim().length() >0 ){
		   norm = getConfigPlotEntry(conn,errors, prop, finder, norm_tag, null, null);
	   }
	   // Plot range properties
	   PropertyTag<Date> start_prop=findDatePropertyTag(conn,finder,prop,tag+"start",start);
	   PropertyTag<Date> end_prop=findDatePropertyTag(conn,finder,prop,tag+"end",end);
	   if( end_prop == null ){
		   end_prop = StandardProperties.ENDED_PROP;
	   }
	   if( plot == null ){
		   return null;
	   }
	   
	   PlotEntry pe = new PlotEntry(plot,norm,start_prop,end_prop,name,desc);
	   //set the reduction type if set
	   String reduction = prop.getProperty(tag+"reduction");
	   if(reduction != null ){
		   Reduction red = Reduction.valueOf(reduction);
		   pe.setReduction(red);
	   }
	   // legacy scale value better to do this with a PropExpression
	   pe.setScale(prop.getDoubleProperty(tag+"scale", pe.getScale()));
	  
	   pe.setLabel(prop.getProperty(tag+"label", pe.getLabel()));
      
	   // Plot range properties
	   pe.setCutoff(prop.getLongProperty(tag+"cutoff", pe.getCutoff()));	   
	   
	   // Rate scale plots
	   pe.setRateScale(prop.getBooleanProperty(tag+"ratescale", pe.isRateScale()));	   
	   pe.setTimeScale(prop.getDoubleProperty(tag+"ratescale.scale",pe.getTimeScale()));
	   pe.setTimeUnit(prop.getProperty(tag+"ratescale.label", pe.getTimeUnit()));
	   
	  
	  
	   
	   return pe;
	 
   }
   private static PropertyTag<Date> findDatePropertyTag(AppContext c,PropertyFinder finder,
		Properties prop, String string,String fallback) {
	String name = prop.getProperty(string,fallback);
	if( name == null ){
		return null;
	}
	@SuppressWarnings("unchecked")
	PropertyTag<Date> t =  (PropertyTag<Date>) finder.find(Date.class, name);
	if( t == null ){
		return null;
	}
	return t;
}
public static Set<PlotEntry> getMappers(AppContext c,UsageProducer up){
	   // force load of properties
	   PropertyFinder finder = up.getFinder();
	   Set<PlotEntry> res = new LinkedHashSet<PlotEntry>();
	   for(PlotEntry e: getPlotSet(finder,c)){
		   if( e.compatible(up)){
			   res.add(e);
		   }
	   }
	   return res;
   }
@Override
public String toString() {
	String desc = getDescription();
	if( desc != null && desc.trim().length() > 0){
		return desc;
	}
	String name = getName();
	if( name != null && name.trim().length() > 0){
		return name;
	}
	return super.toString();
}

}