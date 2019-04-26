//| Copyright - The University of Edinburgh 2015                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
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
	public DocumentFragment addChart(Chart chart, String caption){
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		return result;
	}

	@Override
	public boolean graphOutput() {
		return false;
	}

}