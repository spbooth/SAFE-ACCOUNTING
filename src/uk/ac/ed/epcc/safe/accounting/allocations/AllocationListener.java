package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
/** Interface for objects that need to track changes to allocations 
 * or may want to veto certain operations
 * @author spb
 *
 * @param <T>
 */
public interface AllocationListener<T extends AllocationFactory.AllocationRecord> {
	
	/** An allocation is about to be deleted
	 * 
	 * @param rec AllocationRecord
	 */
	public void deleted(T rec);
	/** Does the listener object to a new allocation with the proposed values.
	 * 
	 * @param values  {@link PropertyContainer} defining proposed object.
	 * @throws ListenerObjection
	 */
	public void canCreate(PropertyContainer values) throws ListenerObjection;
	/** An allocation has been created
	 * 
	 * @param rec AllocationRecord
	 */
	public void created(T rec);
	
	/** Does the listener object to a  allocation modification with the proposed values.
	 * @param rec  allocation being modified
	 * @param values  {@link PropertyContainer} defining proposed object.
	 * @throws ListenerObjection
	 */
	public void canModify(T rec, PropertyContainer values) throws ListenerObjection;

	/** The allocation has been created or modified
	 * 
	 * @param rec AllocationRecord
	 * @param details String description of change
	 */
	public void modified(T rec,String details);
	/** The allocation has been split in two.
	 * 
	 * @param before AllocationRecord first (original) in sequence
	 * @param after AllocationRecord next in sequence
	 */
	public void split(T before, T after);
	/** Does the listener object to a merge of two allocations.
	 * 
	 * @param before AllocationRecord first in sequence (modified)
	 * @param after AllocationRecord next in sequence (removed)
	 * @throws ListenerObjection
	 */
	public void canMerge(T before, T after) throws ListenerObjection;

	/** Two allocations have been combined in two.
	 * 
	 * @param before AllocationRecord first in sequence (modified)
	 * @param after AllocationRecord next in sequence (removed)
	 */
	public void merge(T before, T after);
	/** A
	 * 
	 * @param rec AllocationRecord
	 * @param props PropertyContainer usage record being changed
	 * @param add boolean true for add false for delete
	 */
	public void aggregate(T rec, PropertyContainer props, boolean add);
}
