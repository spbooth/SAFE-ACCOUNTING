package uk.ac.ed.epcc.safe.accounting.reports;

import org.w3c.dom.*;
/** A {@link DomTransform} that implements transforms derived from the Identity transform.
 * 
 * The logic for the identity transform is implemented as default methods so it can be inherited by 
 * multiple sub-classes. The tranformation of specific elements can be overidden by replacing the
 * {@link #wantReplace(Element)} and {@link #replace(Element)} methods.
 * 
 * @author Stephen Booth
 *
 */
public interface IdentityDomTransform extends DomTransform {
	public abstract void setDocument(Document doc);
	public abstract Document getDocument();
	@Override
	default public void transform(Document source, Document destination) {
		setDocument(destination);
		destination.appendChild(transformElement(source.getDocumentElement()));
	}
	default public Node transformNode(Node source) {
		switch(source.getNodeType()) {
		case Node.ELEMENT_NODE: return transformElement((Element)source);
		case Node.DOCUMENT_FRAGMENT_NODE: return transformNodeList(source.getChildNodes());
		default: 
			return getDocument().importNode(source,true);
		}
	}
	default public Node transformNodeList(NodeList content) {
		DocumentFragment result = getDocument().createDocumentFragment();
		for( int i = 0 ; i < content.getLength() ; i++) {
			Node item = content.item(i);
			if( item.getNodeType() != Node.ATTRIBUTE_NODE) {
				Node n = transformNode(item);
				if( n != null ) {
					result.appendChild(n);
				}
			}
		}
		return result;
	}
	default public Node transformElementSet(ElementSet content) {
		DocumentFragment result = getDocument().createDocumentFragment();
		for(Element e : content) {
			Node n =  transformElement(e);
			if( n != null) {
				result.appendChild(n);
			}
		}
		return result;
	}
	default public Node transformSubElementContents(Element parent,String name) {
		DocumentFragment result = getDocument().createDocumentFragment();
		NodeList content  = parent.getChildNodes();
		
		for( int i = 0 ; i < content.getLength() ; i++) {
			Node item = content.item(i);
			if( item.getNodeType() == Node.ELEMENT_NODE && item.getLocalName().equals(name) && (item.getNamespaceURI() == parent.getNamespaceURI())) {
				
				result.appendChild(transformNodeList(item.getChildNodes()));
			}
		}
		return result;
	}
	
	default public boolean wantReplace(Element e) {
		return false;
	}
	default public Node replace(Element e) {
		return null;
	}
	default public Node transformElement(Element source) {
		if( wantReplace(source)) {
			return replace(source);
		}else {
			Element new_element = (Element) source.cloneNode(false);
			getDocument().adoptNode(new_element);
			NodeList children = source.getChildNodes();
			if(children != null) {
				for( int i=0 ; i < children.getLength(); i++) {
					Node c = transformNode(children.item(i));
					if( c != null ) {
						new_element.appendChild(c);
					}
				}
			}
			return new_element;
			
		}
	}
}
