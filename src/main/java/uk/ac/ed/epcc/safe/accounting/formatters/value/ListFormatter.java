package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.util.Collection;

public class ListFormatter implements ValueFormatter<Collection> {

	public ListFormatter() {
	}

	@Override
	public Class<Collection> getType() {
		return Collection.class;
	}

	@Override
	public String format(Collection object) {
		StringBuilder sb = new StringBuilder();
		boolean seen=false;
		for(Object o : object) {
			if( seen) {
				sb.append(",");
			}
			sb.append(o.toString());
			seen=true;
		}
		return sb.toString();
	}

}
