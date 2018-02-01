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
import uk.ac.ed.epcc.webapp.timer.TimerService;



public class ReportTemplate extends DataObject implements Retirable{

	static final String REPORT_NAME = "ReportName";
	static final String REPORT_DESCRIPTION = "ReportDescription";
	static final String TEMPLATE_NAME = "TemplateName";
	static final String REPORT_GROUP = "ReportGroup";
	
	
	
	
	public ReportTemplate(Repository.Record res) {
		super(res);
	}
	private ReportBuilder builder=null;
	private ReportBuilder getBuilder() throws Exception{
		if( builder == null ){
			AppContext conn = getContext();
			builder = ReportBuilder.getInstance(conn);
			ReportBuilder.setTemplate(conn, builder, getTemplateName());
			
		}
		return builder;
	}
	public boolean canUse(SessionService user){
		TimerService timer = getContext().getService(TimerService.class);
		if( timer != null ) {
			timer.startTimer("ReportTemplate.canUse");
		}
		boolean res=false;
		try {
			ReportBuilder b;
			b = getBuilder();
			Map<String,Object> params = new HashMap<String, Object>();
			// Only need the minimum set of extensions needed to check access
			// so pass a null ReportType
			b.setupExtensions(null,params);
			res = b.canUse(user,params);
		} catch (Exception e) {
			getLogger().error("Error creating builder",e);
		}finally {
			if( timer != null ) {
				timer.stopTimer("ReportTemplate.canUse");
			}
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
			getLogger().error("Error creating builder",e);
		}
		return true;
	}
	public String getReportName() {
		return getContext().expandText(record.getStringProperty(REPORT_NAME));
	}
	
	public String getReportDescription() {
		return getContext().expandText(record.getStringProperty(REPORT_DESCRIPTION));
		
	}
	
	public String getReportGroup() {
		return record.getStringProperty(REPORT_GROUP, null);
	}
	
	public String getTemplateName() {
		return record.getStringProperty(TEMPLATE_NAME);
		
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
	public void setName(String name) {
		record.setProperty(REPORT_NAME, name);
		
	}
	public void setDescription(String desc) {
		record.setProperty(REPORT_DESCRIPTION, desc);
		
	}
	public void setTemplate(String template) {
		record.setProperty(TEMPLATE_NAME, template);
		
	}
	public void setGroup(String group){
		record.setOptionalProperty(REPORT_GROUP, group);
	}
	
}