package uk.ac.ed.epcc.safe.accounting.reports;

import java.text.NumberFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import uk.ac.ed.epcc.webapp.AppContext;

/** A chart extension that just supresses the charts
 * 
 * @author spb
 *
 */
public class NullChartExtension extends ChartExtension {

	public NullChartExtension(AppContext c, NumberFormat nf)
			throws ParserConfigurationException {
		super(c, nf);
		
	}

	@Override
	public DocumentFragment addChart(Chart chart, String caption)
			throws Exception {
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		return result;
	}

	@Override
	public boolean graphOutput() {
		return false;
	}

}
