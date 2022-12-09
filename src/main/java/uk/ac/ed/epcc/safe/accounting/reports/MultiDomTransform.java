package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
/** A container to combine multiple {@link IdentityDomTransform}s into
 * a single pass. Assuming that they override disjoint sets of elements.
 * 
 * @author Stephen Booth
 *
 */
public class MultiDomTransform implements IdentityDomTransform {

	private Set<IdentityDomTransform> transforms = new LinkedHashSet<>();
	private Document doc=null;
	
	public MultiDomTransform() {
		
	}
	
	public void addTransform(IdentityDomTransform t) {
		transforms.add(t);
		if( doc != null) {
			t.setDocument(doc);
		}
	}

	@Override
	public void setDocument(Document doc) {
		this.doc=doc;
		for(IdentityDomTransform t : transforms) {
			t.setDocument(doc);
		}
	}

	@Override
	public Document getDocument() {
		return doc;
	}

	@Override
	public boolean wantReplace(Element e) {
		for(IdentityDomTransform t : transforms) {
			if( t.wantReplace(e)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Node replace(Element e) {
		for(IdentityDomTransform t : transforms) {
			if( t.wantReplace(e)) {
				return t.replace(e);
			}
		}
		return null;
	}

}
