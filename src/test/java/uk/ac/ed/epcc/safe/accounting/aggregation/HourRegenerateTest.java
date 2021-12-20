package uk.ac.ed.epcc.safe.accounting.aggregation;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;

public class HourRegenerateTest extends WebappTestBase {

	public HourRegenerateTest() {
		
	}
	
	/** This is a regression test for a bug where the regeneration loop got stuck
	 * at the hour change.
	 * 
	 */
	@Test
	public void testTimeSequence() {
		HourlyAggregateUsageRecordFactory fac = new HourlyAggregateUsageRecordFactory(getContext(), "test");
		
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0L);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.MONTH, Calendar.NOVEMBER);
		c.set(Calendar.YEAR,2016);
		Date end = c.getTime();
		c.add(Calendar.MONTH, -2);
		Date start = c.getTime();
		
		
		Date p_end=end;
		Date p_start=fac.mapStart(p_end);
		int i=0;
		while(p_end.after(start)){
			if( ! p_start.before(p_end)) {
				throw new ConsistencyError("Invalid regenerate period "+p_start+" "+p_end);
			}
			System.out.println("Period is ["+p_start+"] to ["+p_end+"] epoch: "+start);
			
			Date old_start = p_end=p_start;
			p_start=fac.mapStart(p_end);
			i++;
			assertTrue(i<2000);
		}
		
	}

}
