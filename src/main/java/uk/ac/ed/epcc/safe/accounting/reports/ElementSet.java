package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.TreeSet;
import java.util.function.Predicate;

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
	
	
	/** Select children of an {@link Element} using a {@link Predicate} and add to an existing
	 * {@link ElementSet}
	 * 
	 * @param set
	 * @param target
	 * @param rule
	 * @return
	 */
	public static ElementSet select(ElementSet set,Element target, Predicate<Element> rule) {
		NodeList children = target.getChildNodes();
		for(int i=0 ; i< children.getLength();i++) {
			Node child = children.item(i);
			if( child.getNodeType()==Node.ELEMENT_NODE ) {
				Element e =(Element)child;
				if( rule.test(e))  {
					set.add(e);
				}
			}
		}
		return set;
	}
	/** Select all child elements of this {@link ElementSet} filtered using a {@link Predicate}.
	 * adding the results to an existing {@link ElementSet}
	 * @param set
	 * @param rule
	 * @return
	 */
	public ElementSet select(ElementSet set,Predicate<Element> rule) {
		for(Element e : this) {
			select(set,e,rule);
		}
		return set;
	}
	/** Select all child elements of this {@link ElementSet} filtered using a {@link Predicate}.
	 * returning a new {@link ElementSet}
	 * @param rule
	 * @return
	 */
	public ElementSet select(Predicate<Element> rule) {
		return select(new ElementSet(),rule);
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
	public static ElementSet select(Element p,Predicate<Element> rule) {	
		return select(new ElementSet(),p,rule);
	}
}
