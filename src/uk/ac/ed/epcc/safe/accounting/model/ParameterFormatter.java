package uk.ac.ed.epcc.safe.accounting.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.time.CalendarFieldSplitPeriod;
import uk.ac.ed.epcc.webapp.time.RegularSplitPeriod;
import uk.ac.ed.epcc.webapp.time.SplitPeriod;
import uk.ac.ed.epcc.webapp.time.TimePeriod;

public class ParameterFormatter 
{
	private final static String SEPARATOR = "-";
	
	protected static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");

	private ValueParserPolicy parse_vis;
	
	
	public ParameterFormatter(AppContext context) {
		parse_vis = new ValueParserPolicy(context);
	}

	
	public static String format(TimePeriod p){
		StringBuilder result = new StringBuilder();
		String start = timestampFormat.format(p.getStart());
		result.append(start);
		
		if( p instanceof SplitPeriod){
			if(p instanceof CalendarFieldSplitPeriod){
				CalendarFieldSplitPeriod cp = (CalendarFieldSplitPeriod)p;
				result.append(SEPARATOR).append(cp.getField()); // split unit
				result.append(SEPARATOR).append(cp.getCount()); // number of split units
				result.append(SEPARATOR).append(cp.getNsplit()); // number of splits
			}
			else if( p instanceof RegularSplitPeriod){
				RegularSplitPeriod rp = (RegularSplitPeriod)p;
				result.append(SEPARATOR).append(timestampFormat.format(rp.getEnd()));
				result.append(SEPARATOR).append(rp.getNsplit());
			}else{
				throw new IllegalArgumentException(p.getClass().getCanonicalName()+" not supported");
			}
		}else{
			result.append(SEPARATOR).append(p.getEnd());
		}
		return result.toString();
	}
	
	public static String format(Date date) {
		return dateFormat.format(date);
	}
	
	public static String format(Indexed ind) {
		return String.valueOf(ind.getID());
	}
	
	public static String format(Object value)
	{
		if (value instanceof Date) {
			return format((Date)value);
		}
		else if (value instanceof Indexed) {
			return format((Indexed)value);
		}
		else if (value instanceof TimePeriod) {
			return format((TimePeriod)value);
		}
		else {
			return String.valueOf(value);
		}
	}

}
