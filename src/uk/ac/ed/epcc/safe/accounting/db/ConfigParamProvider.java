package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

/** Interface for classes that register the Configuration parameters they use.
 * 
 * @author spb
 *
 */
public interface ConfigParamProvider {
	public void addConfigParameters(Set<String> params);
}
