package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.safe.accounting.reports.AtomExtension.AtomResult;
import uk.ac.ed.epcc.webapp.time.Period;

/** Interface for custom plugins that generate atom values
 * 
 * @author Stephen Booth
 *
 * @param <N>
 */
public interface AtomPlugin<N> {
   public AtomResult<N> evaluate(Period period,RecordSet set) throws Exception;
}
