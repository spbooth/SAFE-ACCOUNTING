// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.selector;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;

/** RecordSelector that requires a relationship between two PropExpressions
 * 
 * @author spb
 *
 * @param <T>
 */
@uk.ac.ed.epcc.webapp.Version("$Id: RelationClause.java,v 1.3 2014/09/15 14:32:29 spb Exp $")

public final class RelationClause<T> implements RecordSelector {
	public final PropExpression<T> left;
	public final PropExpression<T> right;
	public final MatchCondition match;
	public RelationClause(PropExpression<T> left,MatchCondition match, PropExpression<T> right){
		this.left=left.copy();
		this.right=right.copy();
		this.match=match;
	}
	public RelationClause(PropExpression<T> left, PropExpression<T> right){
		this(left,null,right);
	}
	
	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		
		return visitor.visitRelationClause(this);
	}
	public RelationClause<T> copy() {
		return this;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((match == null) ? 0 : match.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelationClause other = (RelationClause) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (match != other.match)
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

}