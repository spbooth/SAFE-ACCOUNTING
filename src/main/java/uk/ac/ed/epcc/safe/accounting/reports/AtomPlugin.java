package uk.ac.ed.epcc.safe.accounting.reports;

import org.w3c.dom.Element;

import uk.ac.ed.epcc.safe.accounting.reports.AtomExtension.AtomResult;
import uk.ac.ed.epcc.webapp.time.Period;

/** Interface for custom plugins that generate atom values
 * 
 * @author Stephen Booth
 *
 * @param <N>
 */
public interface AtomPlugin<N> {
   public AtomResult<N> evaluate(AtomExtension ext,Element element,Period period,RecordSet set) throws Exception;
}
