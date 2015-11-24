// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
/** Formats a period as concise human readable text.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ShortTextPeriodFormatter.java,v 1.10 2014/09/15 14:32:23 spb Exp $")
@Description("Formats a period as concise human readable text")
public class ShortTextPeriodFormatter implements ValueFormatter<TimePeriod> {

	public Class<TimePeriod> getType() {
		return TimePeriod.class;
	}

	




	
	public String format(TimePeriod tp){
		if( tp == null ){
			return "";
		}
		String format="";
		boolean show_end=true;
		Calendar start = Calendar.getInstance();
		start.setTime(tp.getStart());
		Calendar add_quarter=Calendar.getInstance();
		add_quarter.setTime(tp.getStart());
		add_quarter.add(Calendar.MONTH,3);
		Calendar end = Calendar.getInstance();
		end.setTime(tp.getEnd());
		if( start.get(Calendar.MILLISECOND) != 0 || end.get(Calendar.MILLISECOND) != 0 ||
				start.get(Calendar.SECOND) != 0 || end.get(Calendar.SECOND) != 0 ||
				start.get(Calendar.HOUR_OF_DAY) != 0 || end.get(Calendar.HOUR) != 0 ){
			format="HH:mm dd-MMM";
		}else if( start.get(Calendar.DAY_OF_MONTH) != 1 || end.get(Calendar.DAY_OF_MONTH) != 1){
			format="dd-MMM";
			end.add(Calendar.DAY_OF_YEAR, -1);
			if( start.equals(end)){
				show_end=false;
			}
		}else if (start.get(Calendar.MONTH)%3 == 0 && end.equals(add_quarter)){
			format="'Q"+((start.get(Calendar.MONTH)/3)+1)+"'";
			show_end=false;
		}else if( start.get(Calendar.MONTH) != Calendar.JANUARY || end.get(Calendar.MONTH)!=Calendar.JANUARY){
			format="MMM";
			// pull end back and print end-value. 
			// Normal convension is that month ranges are inclusive
			end.add(Calendar.MONTH,-1);
			if( start.equals(end)){
				show_end=false;
			}
		}else{
			end.add(Calendar.YEAR,-1);
			if( start.equals(end)){
				show_end=false;
			}
		}
		boolean same_year=start.get(Calendar.YEAR)==end.get(Calendar.YEAR);
		DateFormat start_df;
		if( ! same_year || ! show_end){
			start_df = new SimpleDateFormat(format+" yyyy");
		}else{
			start_df = new SimpleDateFormat(format);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(start_df.format(tp.getStart()));
		if( show_end ){
			DateFormat end_df = new SimpleDateFormat(format+" yyyy");
			sb.append(" - ");
			// print the modified end time as we expect ranges to be inclusive
			sb.append(end_df.format(end.getTime()));
		}
		return sb.toString();
		
	}

}