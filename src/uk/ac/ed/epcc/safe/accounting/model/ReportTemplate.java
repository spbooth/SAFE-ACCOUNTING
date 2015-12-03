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
package uk.ac.ed.epcc.safe.accounting.model;

import java.util.HashMap;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.table.IntegerFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository;
import uk.ac.ed.epcc.webapp.model.data.Retirable;
import uk.ac.ed.epcc.webapp.session.SessionService;



public class ReportTemplate extends DataObject implements Retirable{

	static final String REPORT_NAME = "ReportName";
	static final String REPORT_DESCRIPTION = "ReportDescription";
	static final String TEMPLATE_NAME = "TemplateName";

	private ReportBuilder builder=null;
	
	
	public ReportTemplate(Repository.Record res) {
		super(res);
	}
	
	private ReportBuilder getBuilder() throws Exception{
		if( builder == null ){
			AppContext conn = getContext();
			builder = ReportBuilder.getInstance(conn);
			ReportBuilder.setTemplate(conn, builder, getTemplateName());
			
		}
		return builder;
	}
	public boolean canUse(SessionService user){
		boolean res=false;
		try {
			ReportBuilder b = getBuilder();
			Map<String,Object> params = new HashMap<String, Object>();
			b.setupExtensions(params);
			res = b.canUse(user,params);
		} catch (Exception e) {
			getContext().error(e,"Error creating builder");
		}
		getContext().getService(LoggerService.class).getLogger(getClass()).debug("Template "+getReportName()+" canUse="+res);
		return res;
	}
	/** Check if there is an error in the report
	 * additional checks may be made depending on the role of
	 * the user.
	 * 
	 * @param user
	 * @return boolean
	 */
	public boolean hasError(SessionService user){
		try {
			return getBuilder() == null;
		} catch (Exception e) {
			getContext().error(e,"Error creating builder");
		}
		return true;
	}
	public String getReportName() {
		return record.getStringProperty(REPORT_NAME);
	}
	
	public String getReportDescription() {
		return record.getStringProperty(REPORT_DESCRIPTION);
		
	}
	
	public String getTemplateName() {
		return record.getStringProperty(TEMPLATE_NAME);
		
	}

	public static TableSpecification getDefaultTableSpecification(AppContext c){
		TableSpecification s = new TableSpecification();
		s.setField(REPORT_NAME, new StringFieldType(false, null, c.getIntegerParameter("report.template.name.length", 64)));
		s.setField(TEMPLATE_NAME, new StringFieldType(false, null, c.getIntegerParameter("report.template.path.length", 128)));
		s.setField(REPORT_DESCRIPTION, new StringFieldType(false, null, c.getIntegerParameter("report.template.description.length", 255)));
		s.setOptionalField(ReportTemplateFactory.SORT_PRIORITY, new IntegerFieldType(false, 50));
	
		return s;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.DataObject#getIdentifier()
	 */
	@Override
	public String getIdentifier(int max) {
		return getReportName();
	}

	public boolean canRetire() {
		return true;
	}

	public void retire() throws Exception {
		delete();
	}
	
}