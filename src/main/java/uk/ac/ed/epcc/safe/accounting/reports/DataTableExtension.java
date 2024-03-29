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

import javax.xml.parsers.ParserConfigurationException;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.TableFormatPolicy;

public class DataTableExtension extends TableExtension {

	public DataTableExtension(AppContext conn, ReportType type)
			throws ParserConfigurationException {
		super(conn, type);
	}

	@Override
	protected Class<? extends TableFormatPolicy> getDefaultTableFormatPolicy() {
		return uk.ac.ed.epcc.webapp.content.TableXMLDataFormatter.class;
	}

}