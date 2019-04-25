package uk.ac.ed.epcc.safe.accounting.parsers;


import java.util.regex.Pattern;

/** A {@link ContainerEntryMaker} for composite entries
 * that contain multiples values given as comma separated name=value pairs
 * 
 * The valueString is split and then parsed by a nested {@link MakerMap}
 * 
 * @author spb
 *
 */
public class NestedContainerEntryMaker extends AbstractNestedContainerEntryMaker {

	public NestedContainerEntryMaker(MakerMap map) {
		super(map);
	}
	private static final Pattern ATTR_PATTERN=Pattern.compile("(\\w+)=([^,\\s]*)");
	/**
	 * @return the attrPattern
	 */
	protected Pattern getAttrPattern() {
		return ATTR_PATTERN;
	}

}
