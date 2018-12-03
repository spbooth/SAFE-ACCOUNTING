package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;

/** combined set of data needed for an upload tests
 * 
 * @author Stephen Booth
 *
 */
public interface UploadContext {
	
	/** data that is exptected to parse
	 * 
	 * @return
	 */
	public String getUpdateText();
	
	/** data where every record is expected to generate an exception
	 * 
	 * @return
	 */
	public String getExceptionText();
	
	/** data where every record is expected to be skipped silently,
	 * 
	 * @return
	 */
	public String getSkipText();
	
	/** default values to use in parse method tests
	 * 
	 * @return
	 */
	public PropertyMap getDefaults();
	
	/** Get the default post params for an end-to-end upload test
	 * 
	 * @return
	 */
	public Map<String,Object> getDefaultParams();
	
	
	
	/** name of a resoruce containing the expected database state after end-to-end upload test
	 * 
	 * @return
	 */
	public String getExpectedResource();
	
	
}
