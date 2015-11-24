package uk.ac.ed.epcc.safe.accounting.properties;
/** policy to select a {@link PropertyTag}.
 * 
 * @author spb
 *
 */
public interface TagFilter {
	public boolean accept(PropertyTag tag);
}
