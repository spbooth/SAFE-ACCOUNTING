package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.HashMap;

import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
/** A map from keywords to {@link ContainerEntryMaker}
 * 
 * @author spb
 *
 */
public class MakerMap extends HashMap<String, ContainerEntryMaker> {
	/**
	 * Convenience method for adding a PropertyEntryMaker to a MakerMap. Used in
	 * the numerous static blocks within this class.
	 * 
	 * @param <T>
	 *          The type of value the maker will parse value strings into
	 * @param tag
	 *          The property tag to be associated with the values generated
	 * @param parser
	 *          The parser used to generate values from value strings
	 */
	public <T> ContainerEntryMaker addParser(PropertyTag<T> tag, ValueParser<? extends T> parser){
		return addParser(tag.getName(),tag, parser);
	}
	/**
	 * Convenience method for adding a PropertyEntryMaker to a MakerMap. Used in
	 * the numerous static blocks within this class.
	 * 
	 * @param <T>
	 *          The type of value the maker will parse value strings into
	 * @param name 
	 * 			The attribute name to store the entry.
	 * @param tag
	 *          The property tag to be associated with the values generated
	 * @param parser
	 *          The parser used to generate values from value strings
	 */
	public <T> ContainerEntryMaker addParser(String name,PropertyTag<T> tag, ValueParser<? extends T> parser){
		PropertyEntryMaker<T> propertyMaker = new PropertyEntryMaker<T>(tag, parser);
		ContainerEntryMaker maker = (ContainerEntryMaker) propertyMaker;
		return put(name, maker);
	}
}