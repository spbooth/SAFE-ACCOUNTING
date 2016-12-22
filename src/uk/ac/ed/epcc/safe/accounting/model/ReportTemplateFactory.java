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
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.forms.result.ChainedTransitionResult;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderClause;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.FilterResult;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataNotFoundException;
import uk.ac.ed.epcc.webapp.model.data.filter.SQLValueFilter;
import uk.ac.ed.epcc.webapp.model.data.table.TableStructureDataObjectFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;



public class ReportTemplateFactory<R extends ReportTemplate> extends TableStructureDataObjectFactory<R> {
	static final String SORT_PRIORITY = "SortPriority";
	private final ReportGroups reportGroups;

	public ReportTemplateFactory(AppContext c) {
		this(c, "ReportTemplate");
	}
	public ReportTemplateFactory(AppContext c,String table) {
		setContext(c, table);
		reportGroups = new ReportGroups(c);
	}
	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c,String table){
		return ReportTemplate.getDefaultTableSpecification(c);
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
	
	public FilterResult<R> getTemplatesInGroup(String group) {
		try {
			if (group == null || !res.hasField(ReportTemplate.REPORT_GROUP)) {
				return all();
			}
			else {
				return getResult(new SQLValueFilter<R>(getTarget(), res, ReportTemplate.REPORT_GROUP, group));
			}
		} catch (DataFault e) {
			return null;
		}	
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
	
	public class ReportGroups {
		
		private Set<String> groups = new HashSet<String>(); 
		
		public ReportGroups(AppContext conn) {
			String names = conn.getInitParameter("report_groups");
			if (names != null && names.trim().length() != 0) {
				Collections.addAll(groups, names.trim().split("\\s*,\\s*"));
			}
		}
		
		public Set<String> getGroups() {
			return groups;
		}
		
		public boolean isGroup(String name) {
			return groups.contains(name);
		}
		
	}
	
	@Override
	protected Map<String, Object> getSelectors() {
		Map<String,Object>result = new HashMap<String,Object>();
		result.put(ReportTemplate.TEMPLATE_NAME, new TemplateNameInput());
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
		return getReportGroupTable(null);
	}

	public Table<String,ReportTemplate> getReportGroupTable(String group) throws DataFault
	{
		Table<String,ReportTemplate> t = new Table<String,ReportTemplate>();
		ReportTemplateTransitionProvider prov = new ReportTemplateTransitionProvider(getContext());
		SessionService sess = getContext().getService(SessionService.class);

		FilterResult<R> reportTemplates = getTemplatesInGroup(group);
		if (reportTemplates == null) {
			getLogger().debug("Problem listing report templates");
		}
		else {
			for (ReportTemplate reportTemplate : reportTemplates) {	
				if (reportTemplate.canUse(sess))
				{
					String reportName = reportTemplate.getReportName();
					String reportDescription = reportTemplate.getReportDescription();
					t.put("Name", reportTemplate, reportName);
					t.put("Description", reportTemplate, reportDescription);
					t.put("Generate", reportTemplate, new Link(getContext(), "Generate", 
							new ChainedTransitionResult<Report, ReportTemplateKey>(
									prov, new Report(reportTemplate), null)));
				}
			}
		}
		return t;
	}

	
}