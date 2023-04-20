package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.parsers.value.*;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
/** An {@link AbstractKeyPairParser} that keys directly of property names.
 * 
 * This is primarily intended for use in tests but can also be used in contexts where
 * an intermediate format is required or if the uploading process needs to be responsible for the mapping to properties
 * 
 * @author Stephen Booth
 *
 */
public class DirectParser extends AbstractKeyPairParser {

	public DirectParser(AppContext conn) {
		super(conn);
	}

	private MakerMap maker = new MakerMap();
	private PropertyFinder finder;
    private String table;
	@Override
	protected ContainerEntryMaker getEntryMaker(String attr) {
		ContainerEntryMaker result = maker.get(attr);
		if( result != null) {
			return result;
		}
		
		PropertyTag tag = finder.find(attr);
		if( tag == null ) {
			return null;
		}
		if( tag instanceof ValueParserProvider) {
			result = new PropertyEntryMaker<>(tag, ((ValueParserProvider)tag).getValueParser(getContext()));
		}else {
			try {
				ValueParserPolicy pol = new ValueParserPolicy(getContext());
				String format = getContext().getInitParameter(table+"."+attr+".parse_format");
				if( format != null) {
					pol.setFormat(format);
				}
				result = new PropertyEntryMaker(tag, (ValueParser)tag.accept(pol));
			}catch(Exception e) {
				getLogger().error("Error making ValueParser", e);
			}
		}
		if( result != null ) {
			maker.put(attr,result);
		}
		return result;
	}
	
	@Override
	public PropertyFinder initFinder(PropertyFinder prev, String table) {
		finder = super.initFinder(prev, table);
		this.table=table;
		return finder;
	}

}
