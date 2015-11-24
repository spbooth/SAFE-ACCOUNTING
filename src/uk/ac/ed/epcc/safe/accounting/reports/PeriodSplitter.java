// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.webapp.time.SplitPeriod;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
@uk.ac.ed.epcc.webapp.Version("$Id: PeriodSplitter.java,v 1.7 2014/09/29 08:36:21 spb Exp $")

/** A {@link Splitter} that breaks a {@link SplitPeriod} into its sub-periods.
 * 
 * This is needed 
 * 
 * @author spb
 *
 */
public class PeriodSplitter implements Splitter<SplitPeriod,TimePeriod> {

	
	public TimePeriod[] split(SplitPeriod input) {
		if( input == null ){
			return null;
		}
		return input.getSubPeriods();
	}

}