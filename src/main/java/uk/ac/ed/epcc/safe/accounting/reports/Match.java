package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.function.Predicate;

import org.w3c.dom.Element;
/** A {@link Predicate} on {@link Element}s
 * roughly equivalent to an XPath name select clause
 * 
 * @author Stephen Booth
 *
 */
public class Match implements Predicate<Element> {
	public Match(String ns, String name) {
		super();
		this.ns = ns;
		this.tag = name;
	}
	private final String ns;
	private final String tag;
	@Override
	public boolean test(Element e) {
		return (ns==null || ns.equals(e.getNamespaceURI()) && (tag.equals("*") || tag.equals(e.getLocalName())));
	}
	
	public static Predicate<Element> match(String ns, String ... names ) {
		Predicate<Element> result = null;
		for(String n : names) {
			if( result == null) {
				result = new Match(ns,n);
			}else {
				result.or(new Match(ns, n));
			}
		}
		return result;
		
	}
}
