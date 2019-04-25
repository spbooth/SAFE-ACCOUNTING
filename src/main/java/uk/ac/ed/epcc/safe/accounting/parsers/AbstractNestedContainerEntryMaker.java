package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;

public abstract class AbstractNestedContainerEntryMaker implements ContainerEntryMaker  {

	protected final MakerMap maker_map;

	public AbstractNestedContainerEntryMaker(MakerMap map) {
		this.maker_map=map;
	}
	protected abstract Pattern getAttrPattern();
	@Override
	public void setValue(PropertyContainer contanier, String valueString)
			throws IllegalArgumentException, InvalidPropertyException, NullPointerException, AccountingParseException {
				Matcher m = getAttrPattern().matcher(valueString);
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
				Matcher m = getAttrPattern().matcher(valueString);
				while(m.find()) {
					ContainerEntryMaker maker = maker_map.get(m.group(1));
					if( maker != null ) {
						maker.setValue(map, m.group(2));
					}
				}
			
			}

}