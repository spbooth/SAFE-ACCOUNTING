//| Copyright - The University of Edinburgh 2011                            |
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
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DocumentFragment;

import uk.ac.ed.epcc.webapp.AppContext;
/** Generate the table form of a plot for non graphical output
 * 
 * @author spb
 *
 */


public class TableChartExtension extends ChartExtension {

	public TableChartExtension(AppContext c,ReportType type) throws ParserConfigurationException {
		super(c,type);
	}

	
	@Override
	public DocumentFragment addChart(Chart chart,String caption) {
		try {
			return addChartTable(chart, caption);
		} catch (Exception e) {
			addError("Bad Plot", "Error adding table",e);
			return getDocument().createDocumentFragment();
		}
	}


	@Override
	public boolean graphOutput() {
		return false;
	}
}