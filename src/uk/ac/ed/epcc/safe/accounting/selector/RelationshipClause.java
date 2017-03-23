package uk.ac.ed.epcc.safe.accounting.selector;

/** Select records where the current user has the specified relationship with the target record.
 * 
 * @author spb
 *
 */
public class RelationshipClause implements RecordSelector{

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (relationship == null) {
			if (other.relationship != null)
				return false;
		} else if (!relationship.equals(other.relationship))
			return false;
		return true;
	}
	public RelationshipClause(String relationship) {
		super();
		this.relationship = relationship;
	}
	private final String relationship;
	@Override
	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		return visitor.visitRelationshipClause(this);
	}
	@Override
	public RecordSelector copy() {
		return this;
	}
	
	/**
	 * @return the relationship
	 */
	public String getRelationship() {
		return relationship;
	}
	

}
