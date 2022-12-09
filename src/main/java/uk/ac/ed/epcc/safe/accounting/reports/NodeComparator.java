package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.Comparator;

import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
/** A {@link Comparator} for {@link Node}s that sorts in document order.
 * 
 * @author Stephen Booth
 *
 */
public class NodeComparator implements Comparator<Node> {

	public NodeComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Node o1, Node o2) {
		if( o1.equals(o2)) {
			return 0;
		}
		short pos =o1.compareDocumentPosition(o2);
		if( (pos & Node.DOCUMENT_POSITION_FOLLOWING) != 0) {
			return -1;
		}else if( (pos & Node.DOCUMENT_POSITION_PRECEDING) != 0) {
			return 1;
		}
		throw new ConsistencyError("Unexpected relative Node position "+pos);
	}

}
