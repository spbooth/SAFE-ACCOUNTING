package uk.ac.ed.epcc.safe.accounting.allocations;

import java.util.Calendar;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.time.CalendarFieldSplitPeriod;


/** This class represents the current time period that 
 * is being looked at.
 * 
 * @author spb
 *
 */
public class ViewPeriod extends CalendarFieldSplitPeriod{
	private static final String SEPERATOR = "-";
	
	
	public ViewPeriod(CalendarFieldSplitPeriod p){
		this(p.getCalStart(),p.getField(),p.getCount(),p.getNsplit());
	}
	public ViewPeriod(Calendar start, int field, int block, int num_periods) {
		super(start,field,block,num_periods);
	}

	
	  
	
	public static ViewPeriod getViewPeriod(AppContext conn){
		Calendar start = Calendar.getInstance();
		start.set(Calendar.MILLISECOND,0);
		start.set(Calendar.SECOND,0);
		start.set(Calendar.MINUTE,0);
		start.set(Calendar.HOUR_OF_DAY,0);
		start.set(Calendar.DAY_OF_YEAR,1);
		start.add(Calendar.YEAR, conn.getIntegerParameter("default_period.back",-1));
		return new ViewPeriod(start,Calendar.YEAR,1,conn.getIntegerParameter("default_period.length", 3));
	}
	
	public ViewPeriod up(){
		Calendar c = (Calendar) getCalStart().clone();
		c.add(getField(), getCount());
		return new ViewPeriod(c,getField(),getCount(),getNsplit());
	}
	public ViewPeriod down(){
		Calendar c = (Calendar) getCalStart().clone();
		c.add(getField(), - getCount());
		return new ViewPeriod(c,getField(),getCount(),getNsplit());
	}
	
	public static ViewPeriod parsePeriod(String id){
		//System.out.println("<"+id+">");
		String tags[] = id.split(SEPERATOR);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(Long.parseLong(tags[0]));
		return new ViewPeriod(
		c,
		Integer.parseInt(tags[1]),
		Integer.parseInt(tags[2]),
		Integer.parseInt(tags[3]));
	}
	public String toString(){
		return getCalStart().getTimeInMillis()+SEPERATOR+getField()+SEPERATOR+getCount()+SEPERATOR+getNsplit();
	}
	
	
}