package uk.ac.ed.epcc.safe.accounting.selector;

import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;

/** Select records where the current user has the specified relationship with the referenced object.
 * 
 * @author spb
 *
 * @param <D>
 * @param <F>
 */
public class RelationshipClause<D extends DataObject,F extends DataObjectFactory<D>> implements RecordSelector{

	public RelationshipClause(ReferenceTag<D, F> reference, String relationship) {
		super();
		this.reference = reference;
		this.relationship = relationship;
	}
	private final ReferenceTag<D, F> reference;
	private final String relationship;
	@Override
	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public RecordSelector copy() {
		return this;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reference == null) ? 0 : reference.hashCode());
		result = prime * result + ((relationship == null) ? 0 : relationship.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelationshipClause other = (RelationshipClause) obj;
		if (reference == null) {
			if (other.reference != null)
				return false;
		} else if (!reference.equals(other.reference))
			return false;
		if (relationship == null) {
			if (other.relationship != null)
				return false;
		} else if (!relationship.equals(other.relationship))
			return false;
		return true;
	}
	/**
	 * @return the reference
	 */
	public ReferenceTag<D, F> getReference() {
		return reference;
	}
	/**
	 * @return the relationship
	 */
	public String getRelationship() {
		return relationship;
	}
	

}
