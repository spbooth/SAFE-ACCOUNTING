package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;

public class NarrowTag {
	/**
	 * @param tag
	 * @param sel
	 */
	public NarrowTag(String tag, RecordSelector sel) {
		super();
		this.tag = tag;
		this.sel = sel.copy();
	}
	private final String tag;
	private final RecordSelector sel;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sel == null) ? 0 : sel.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
		NarrowTag other = (NarrowTag) obj;
		if (sel == null) {
			if (other.sel != null)
				return false;
		} else if (!sel.equals(other.sel))
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}

}
