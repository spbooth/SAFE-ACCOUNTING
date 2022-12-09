package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.TreeSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
/** A set of {@link Element}s sorted in document order
 * 
 * @author Stephen Booth
 *
 */
public class ElementSet extends TreeSet<Element> {
	public ElementSet() {
		super(new NodeComparator());
	}
	
	/** Merge an {@link Element} or an {@link ElementSet} into this set.
	 * 
	 * @param o
	 * @return modified {@link ElementSet}
	 */
	public ElementSet merge(Object o) {
		if( o instanceof Element) {
			add((Element)o);
			return this;
		}
		if( o instanceof ElementSet) {
			addAll((ElementSet) o);
			return this;
		}
		throw new ConsistencyError("Unsupported type "+o.getClass().getCanonicalName());
	}
	
	/** Select children of an element by name and add to an existing {@link ElementSet}
	 * 
	 * This is intended as part of a pseudo XPATH search so a tag of * will
	 * match all elements
	 * 
	 * @param set
	 * @param target
	 * @param ns
	 * @param tag
	 * @return modified ElementSet
	 */
	public static ElementSet select(ElementSet set,Element target, String ns, String tag) {
		NodeList children = target.getChildNodes();
		for(int i=0 ; i< children.getLength();i++) {
			Node child = children.item(i);
			if( child.getNodeType()==Node.ELEMENT_NODE ) {
				Element e =(Element)child;
				if( (ns==null || ns.equals(e.getNamespaceURI()) && (tag.equals("*") || tag.equals(e.getLocalName()))))  {
					set.add(e);
				}
			}
		}
		return set;
	}
	/** Select all child elements of the {@link ElementSet} members by name.
	 * 
	 * @param set
	 * @param ns
	 * @param tag
	 * @return
	 */
	public ElementSet select(ElementSet set,String ns,String tag) {
		for(Element e : this) {
			select(set,e,ns,tag);
		}
		return set;
	}
	public ElementSet select(String ns,String tag) {
		return select(new ElementSet(),ns,tag);
	}
	
	public static ElementSet ancestors(Element p) {
		ElementSet result = new ElementSet();
		Node parent = p.getParentNode();
		while( parent.getNodeType() == Node.ELEMENT_NODE) {
			result.add((Element) parent);
			parent = parent.getParentNode();
		}
		return result;
	}
	public static ElementSet ancestors_self(Element p) {
		ElementSet result = new ElementSet();
		result.add(p);
		Node parent = p.getParentNode();
		while( parent.getNodeType() == Node.ELEMENT_NODE) {
			result.add((Element) parent);
			parent = parent.getParentNode();
		}
		return result;
	}
	public static ElementSet select(Element p,String ns, String tag) {	
		return select(new ElementSet(),p,ns,tag);
	}
}
