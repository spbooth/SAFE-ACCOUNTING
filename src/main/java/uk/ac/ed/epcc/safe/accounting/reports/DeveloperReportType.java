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

import uk.ac.ed.epcc.webapp.session.SessionService;

public class DeveloperReportType extends ReportType{

	public DeveloperReportType(String name, String extension, String mime,
			String description,String help,String image) {
		super(name, extension, mime, description,help,image);
	}

	@Override
	public boolean allowSelect(SessionService sess) {
		return sess.hasRole(ReportBuilder.REPORT_DEVELOPER);
	}
	
}