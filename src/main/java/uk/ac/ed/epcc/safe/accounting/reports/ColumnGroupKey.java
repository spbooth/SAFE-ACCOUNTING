package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.UIGenerator;

/** A composite table column key for adding a sub-table as a column group.
 * 
 * We re-map the column keys so that multiple tables can add columns with the same name to different groups.
 * 
 * @author Stephen Booth
 *
 * @param <C> type of original key
 */
public class ColumnGroupKey<C> implements UIGenerator{

	public ColumnGroupKey(C original, String group) {
		super();
		this.original = original;
		this.group = group;
	}
	private final C original;
	private final String group;
	@Override
	public ContentBuilder addContent(ContentBuilder builder) {
		builder.addObject(original);
		return builder;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((original == null) ? 0 : original.hashCode());
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
		ColumnGroupKey other = (ColumnGroupKey) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (original == null) {
			if (other.original != null)
				return false;
		} else if (!original.equals(other.original))
			return false;
		return true;
	}
	public String toString() {
		return original.toString();
	}

}
