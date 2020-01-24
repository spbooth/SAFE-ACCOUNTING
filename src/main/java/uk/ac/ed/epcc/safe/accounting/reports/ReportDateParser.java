package uk.ac.ed.epcc.safe.accounting.reports;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;

/** Class that encodes all the various date/time formats supported by the Accounting code
 * 
 * @author Stephen Booth
 *
 */
public class ReportDateParser {
	/**
	 * @param vp
	 */
	public ReportDateParser(ValueParser<Date> vp) {
		super();
		this.vp = vp;
	}

	// Note SimpledateFormat not thread safe so don't make static
	private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
	private final SimpleDateFormat altTimestampFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	private final SimpleDateFormat altDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	private final SimpleDateFormat altMonthFormat = new SimpleDateFormat("MM-yyyy");
	private final SimpleDateFormat fmts[] = {timestampFormat,altTimestampFormat,altDateFormat,dateFormat,altMonthFormat,monthFormat};

	private final ValueParser<Date>vp;
	
	protected void setTime(Calendar time, String timeString) throws Exception {
		
		
		if( timeString.equalsIgnoreCase("Epoch")){
			time.setTimeInMillis(0);
			return;
		}
		if( timeString.equalsIgnoreCase("Now")){
			time.setTimeInMillis(System.currentTimeMillis());
			return;
		}
		if( timeString.equalsIgnoreCase("Forever")){
			time.setTimeInMillis(Long.MAX_VALUE);
			return;
		}
	    
	    timeString=timeString.trim();
	    boolean search=true;
	    for( int i=0; search && i< fmts.length; i++){
	    	SimpleDateFormat df = fmts[i];
    		String pattern = df.toPattern();
	    	try{
	    		// We need to supress the 2 diget year matching
	    		// as this can incorrectly match days or months.
	    		// only accept if length and both hyphens match
	    		// if there is only 1 hyphen we get -1 for both
	    		int pattern_first = pattern.indexOf('-');
				int string_first = timeString.indexOf('-');
				if( pattern.length() == timeString.length() && pattern_first == string_first && pattern.indexOf('-', pattern_first) == timeString.indexOf('-', string_first)){
	    			time.setTime(fmts[i].parse(timeString));
	    			search = false;
	    			break;
	    		}
	    	}catch(Exception e){
	    		throw new uk.ac.ed.epcc.safe.accounting.reports.exceptions.ParseException("Cannot parse "+timeString+" as date/time using "+pattern);
	    	}
	    }
	    if( search ){
	    	try{
	    		if( vp != null ){
	    			time.setTime((Date) vp.parse(timeString.trim()));
	    		}
	    	}catch(Exception e5){
	    		throw new uk.ac.ed.epcc.safe.accounting.reports.exceptions.ParseException("Cannot parse "+timeString+" as date/time");
	    	}
	    }
			
	}
	
	public String format(Date d) {
		return timestampFormat.format(d);
	}
}
