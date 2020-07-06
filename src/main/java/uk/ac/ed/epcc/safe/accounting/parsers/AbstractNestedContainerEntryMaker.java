package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;

public abstract class AbstractNestedContainerEntryMaker implements ContainerEntryMaker  {

	protected final MakerMap maker_map;
    protected final PropertyTag<String> raw_tag;
	public AbstractNestedContainerEntryMaker(PropertyTag<String> raw,MakerMap map) {
		this.raw_tag=raw;
		this.maker_map=map;
	}
	public AbstractNestedContainerEntryMaker(MakerMap map) {
		this(null,map);
	}
	protected abstract Pattern getAttrPattern();
	@Override
	public void setValue(PropertyContainer container, String valueString)
			throws IllegalArgumentException, InvalidPropertyException, NullPointerException, AccountingParseException {
				if( raw_tag != null) {
					container.setOptionalProperty(raw_tag,valueString);
				}
				Matcher m = getAttrPattern().matcher(valueString);
				while(m.find()) {
					ContainerEntryMaker maker = maker_map.get(m.group(1));
					if( maker != null ) {
						maker.setValue(container, m.group(2));
					}
				}
			
			}

	@Override
	public void setValue(PropertyMap map, String valueString)
			throws IllegalArgumentException, NullPointerException, AccountingParseException {
		if( raw_tag != null) {
			map.setOptionalProperty(raw_tag,valueString);
		}
		Matcher m = getAttrPattern().matcher(valueString);
		while(m.find()) {
			ContainerEntryMaker maker = maker_map.get(m.group(1));
			if( maker != null ) {
				maker.setValue(map, m.group(2));
			}
		}

	}

}