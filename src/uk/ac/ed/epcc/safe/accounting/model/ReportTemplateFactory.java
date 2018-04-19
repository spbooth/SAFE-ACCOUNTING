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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.Link;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.forms.exceptions.FieldException;
import uk.ac.ed.epcc.webapp.forms.exceptions.ValidateException;
import uk.ac.ed.epcc.webapp.forms.inputs.SetInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.forms.result.ChainedTransitionResult;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderClause;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLOrFilter;
import uk.ac.ed.epcc.webapp.jdbc.table.IntegerFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.FilterResult;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataNotFoundException;
import uk.ac.ed.epcc.webapp.model.data.filter.SQLValueFilter;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.timer.TimerService;



public class ReportTemplateFactory<R extends ReportTemplate> extends DataObjectFactory<R> {
	

	static final String SORT_PRIORITY = "SortPriority";
	private final ReportGroups reportGroups;

	public ReportTemplateFactory(AppContext c) {
		this(c, "ReportTemplate");
	}
	public ReportTemplateFactory(AppContext c,String table) {
		reportGroups = new ReportGroups(c);
		setContext(c, table);
	}
	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c,String table){
		TableSpecification s = new TableSpecification();
		s.setField(ReportTemplate.REPORT_NAME, new StringFieldType(false, null, c.getIntegerParameter("report.template.name.length", 64)));
		s.setField(ReportTemplate.TEMPLATE_NAME, new StringFieldType(false, null, c.getIntegerParameter("report.template.path.length", 128)));
		s.setField(ReportTemplate.REPORT_DESCRIPTION, new StringFieldType(false, null, c.getIntegerParameter("report.template.description.length", 255)));
		s.setField(ReportTemplate.REPORT_GROUP, new StringFieldType(true, reportGroups.getDefaultGroup(), 32));
		s.setOptionalField(ReportTemplateFactory.SORT_PRIORITY, new IntegerFieldType(false, 50));
		
		return s;
	}
	@Override
	protected ReportTemplate makeBDO(Record res) throws DataFault {
		return new ReportTemplate(res);
	}
	public Class<? super R> getTarget(){
		return ReportTemplate.class;
	}
	@Override
	protected List<OrderClause> getOrder() {
		List<OrderClause> order = super.getOrder();
		if( res.hasField(SORT_PRIORITY)){
			order.add(res.getOrder(SORT_PRIORITY, false));
		}
		order.add(res.getOrder(ReportTemplate.REPORT_NAME, false));
		return order;
	}
	
	public Set<String> getReportGroups() {
		return reportGroups.getGroups();
	}
	/** Get {@link ReportTemplate}s that match specified groups
	 * 
	 * @param group comma separated list of groups.
	 * @return {@link FilterResult}
	 */
	public FilterResult<R> getTemplatesInGroup(String group) {
		TimerService timer = getContext().getService(TimerService.class);
		if(timer != null) {
			timer.startTimer("getTemplatesInGroup:"+group);
		}
		try {
			if (group == null || group.trim().isEmpty() || group.equalsIgnoreCase("all")) {
				return all();
			} else {
				if( ! useGroups()){
					return null;
				}
				SQLOrFilter<R> fil = new SQLOrFilter<R>(getTarget());
				for(String g : group.split("\\s*,\\s*")){
					fil.addFilter(new SQLValueFilter<R>(getTarget(), res, ReportTemplate.REPORT_GROUP, g));
				}
				return getResult(fil);
			}
		} catch (DataFault e) {
			return null;
		}finally {
			if(timer != null) {
				timer.stopTimer("getTemplatesInGroup:"+group);
			}
		}
	}
	/**
	 * @return
	 */
	public boolean useGroups() {
		return res.hasField(ReportTemplate.REPORT_GROUP);
	}
	
	public class TemplateNameInput extends TextInput{

		@Override
		public void validate() throws FieldException {
			super.validate();
			AppContext conn = getContext();
			Logger log = conn.getService(LoggerService.class).getLogger(getClass());
			try {
				new ReportBuilder(conn,getValue(),conn.getInitParameter(ReportBuilder.REPORT_SCHEMA_CONFIG, ReportBuilder.DEFAULT_REPORT_SCHEMA));
			} catch (DataFault e) {
				log.warn("Bad template in ReportTemplate",e);
				throw new ValidateException("Not found",e);
			} catch (URISyntaxException e) {
				log.warn("Bad template in ReportTemplate",e);
				throw new ValidateException("Bad URI in template",e);
			} catch (SAXException e) {
				log.warn("Bad template in ReportTemplate",e);
				// Include parse error in message.
				throw new ValidateException("Cannot parse/validate template "+e.toString(),e);
			} catch (IOException e) {
				getLogger().error("Bad template in ReportTemplate",e);
				throw new ValidateException("Cannot read template",e);
			} catch (ParserConfigurationException e) {
				getLogger().error("Bad template in ReportTemplate",e);
				throw new ValidateException("XML Parser fault",e);
			} catch (InvalidArgument e) {
				getLogger().error("Bad template in ReportTemplate",e);
				throw new ValidateException("Not found",e);
			} catch (TransformerFactoryConfigurationError e) {
				getLogger().error("Bad template in ReportTemplate",e);
				throw new ValidateException("Bad template",e);
			} catch (TransformerException e) {
				getLogger().error("Bad template in ReportTemplate",e);
				throw new ValidateException("Bad template",e);
			}
		}

		public TemplateNameInput() {
			super(false);
		}
		
	}
	
	public static class ReportGroups {
		
		private static final String REPORT_GROUP_INDEX_CONFIG = "report_group_index";
		private static final String DEFAULT_GROUP_CONFIG = "default_report_group";
		private static final String REPORT_GROUPS_CONFIG = "report_groups";
		private final Set<String> groups = new HashSet<String>();
		private final String default_group;
		private final String index_list;
		
		

		public ReportGroups(AppContext conn) {
			String names = conn.getExpandedProperty(REPORT_GROUPS_CONFIG);
			if (names != null && names.trim().length() != 0) {
				Collections.addAll(groups, names.trim().split("\\s*,\\s*"));
			}
			default_group= conn.getInitParameter(DEFAULT_GROUP_CONFIG);
			index_list = conn.getExpandedProperty(REPORT_GROUP_INDEX_CONFIG, default_group);
			
		}
		
		public Set<String> getGroups() {
			return groups;
		}
		
		public boolean isGroup(String name) {
			return groups.contains(name);
		}
		public String getDefaultGroup(){
			return default_group;
		}
		/**
		 * @return the index_list
		 */
		public String getIndexList() {
			return index_list;
		}
	}
	
	@Override
	protected Map<String, Object> getSelectors() {
		Map<String,Object>result = new HashMap<String,Object>();
		result.put(ReportTemplate.TEMPLATE_NAME, new TemplateNameInput());
		result.put(ReportTemplate.REPORT_GROUP, new SetInput<String>(reportGroups.getGroups()));
		return result;
	}
	public R findByFileName(String fileName) throws DataException {
		if(fileName == null){
			return null;
		}
		try {
			return  find(new SQLValueFilter<R>(getTarget(),res,ReportTemplate.TEMPLATE_NAME ,fileName.trim()),true);
			
		} catch (DataNotFoundException e) {
			return null;
		}
	}
	
	public Table<String,ReportTemplate> getIndexTable() throws DataFault
	{
		TimerService timer = getContext().getService(TimerService.class);
		if( timer != null) {
			timer.startTimer("ReportTemplate.getIndexTable");
		}
		try {
		return getReportGroupTable(useGroups() ? reportGroups.getIndexList() : null);
		}finally {
			if( timer != null) {
				timer.stopTimer("ReportTemplate.getIndexTable");
			}
		}
	}

	public Table<String,ReportTemplate> getReportGroupTable(String group) throws DataFault
	{
		return getReportGroupTable(group, null);
	}
	
	public Table<String,ReportTemplate> getReportGroupTable(String group, Map<String, Object> params) throws DataFault
	{
		TimerService timer = getContext().getService(TimerService.class);
		Table<String,ReportTemplate> t = new Table<String,ReportTemplate>();
		SessionService sess = getContext().getService(SessionService.class);

		if( timer != null) {
			timer.startTimer("getTemplatesInGroup");
		}
		FilterResult<R> reportTemplates = getTemplatesInGroup(group);
		if( timer != null) {
			timer.stopTimer("getTemplatesInGroup");
		}
		if (reportTemplates == null) {
			getLogger().debug("No report templates for group: " + group);
			return null;
		}
		else {
			ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(getContext());
			for (ReportTemplate reportTemplate : reportTemplates) {
				if( timer != null) {
					timer.startTimer("add template to group "+reportTemplate.getTemplateName());
				}
				if (reportTemplate.canUse(sess))
				{
					Report report = new Report(reportTemplate.getTemplateName(), params);
					if (params != null) {
						report.setContextParameters(params.keySet());
					}
					String reportName = reportTemplate.getReportName();
					String reportDescription = reportTemplate.getReportDescription();
					t.put("Name", reportTemplate, new Link(getContext(), reportName, 
							new ChainedTransitionResult<Report, ReportTemplateKey>(
									prov, report, ReportTemplateTransitionProvider.PREVIEW)));
					t.put("Description", reportTemplate, reportDescription);
					
				}
				if( timer != null) {
					timer.stopTimer("add template to group "+reportTemplate.getTemplateName());
				}
			}
		}
		return t;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.DataObjectFactory#postCreateTableSetup(uk.ac.ed.epcc.webapp.AppContext, java.lang.String)
	 */
	@Override
	protected void postCreateTableSetup(AppContext c, String table) {
		String list = c.getInitParameter("initial_reports."+table);
		if( list != null && ! list.trim().isEmpty()){
			for(String base : list.split("\\s*,\\s*")){
				if( ! base.isEmpty()){
				try{
					String template = base;
					if( ! template.endsWith(".xml")){
						template=template+".xml";
					}
					if( base.endsWith(".xml")){
						base = base.substring(0, base.indexOf(".xml"));
					}
					String name = c.getInitParameter(base+".name", base);
					String desc = c.getInitParameter(base+".description");
					String group = c.getInitParameter(base+".group");
					ReportTemplate temp = makeBDO();
					temp.setName(name);
					temp.setDescription(desc);
					temp.setTemplate(template);
					temp.setGroup(group);
					temp.commit();
				}catch(Throwable t){
					getLogger().error("Error bootstrapping report "+base, t);
				}
				}
			}
		}
		
	}
}