package uk.ac.ed.epcc.safe.accounting.parsers;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;

/** A {@link ContainerEntryMaker} for composite entries
 * that contain multiples values given as comma separated name=value pairs
 * 
 * The valueString is split and then parsed by a nested {@link MakerMap}
 * 
 * @author spb
 *
 */
public class NestedContainerEntryMaker implements ContainerEntryMaker {

	private final MakerMap maker_map;
	public NestedContainerEntryMaker(MakerMap map) {
		this.maker_map=map;
	}
	static final Pattern ATTR_PATTERN=Pattern.compile("(\\w+)=([^,\\s]*)");
	@Override
	public void setValue(PropertyContainer contanier, String valueString)
			throws IllegalArgumentException, InvalidPropertyException, NullPointerException, AccountingParseException {
		Matcher m = ATTR_PATTERN.matcher(valueString);
		while(m.find()) {
			ContainerEntryMaker maker = maker_map.get(m.group(1));
			if( maker != null ) {
				maker.setValue(contanier, m.group(2));
			}
		}

	}

	@Override
	public void setValue(PropertyMap map, String valueString)
			throws IllegalArgumentException, NullPointerException, AccountingParseException {
		Matcher m = ATTR_PATTERN.matcher(valueString);
		while(m.find()) {
			ContainerEntryMaker maker = maker_map.get(m.group(1));
			if( maker != null ) {
				maker.setValue(map, m.group(2));
			}
		}

	}

}
