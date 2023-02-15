package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
/** A {@link Splitter} that breaks a {@link TimePeriod} into a 
 * series of sub-periods no more than a day in length
 * 
 * @author Stephen Booth
 *
 */
public class DayPeriodSplitter implements Splitter<TimePeriod, TimePeriod> {

	public DayPeriodSplitter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public TimePeriod[] split(TimePeriod input) throws ReportException {
		LinkedList<Period> list = new LinkedList<>();
		Calendar c = Calendar.getInstance();
		c.setTime(input.getStart());
		Date s = c.getTime();
		Date e = input.getEnd();
		while(s.before(e)) {
			c.add(Calendar.DAY_OF_YEAR, 1);
			Date t = c.getTime();
			if( t.before(e)) {
				list.add(new Period(s,t));
				 
			}else {
				list.add(new Period(s,e));  
				break;
			}
			s = t;
		}
		
		return list.toArray(new TimePeriod[list.size()]);
	}

}
