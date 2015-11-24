// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.forms.exceptions.FieldException;
import uk.ac.ed.epcc.webapp.forms.exceptions.ValidateException;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderClause;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataNotFoundException;
import uk.ac.ed.epcc.webapp.model.data.filter.SQLValueFilter;
import uk.ac.ed.epcc.webapp.model.data.table.TableStructureDataObjectFactory;
@uk.ac.ed.epcc.webapp.Version("$Id: ReportTemplateFactory.java,v 1.22 2015/07/17 15:53:01 spb Exp $")


public class ReportTemplateFactory<R extends ReportTemplate> extends TableStructureDataObjectFactory<R> {
	static final String SORT_PRIORITY = "SortPriority";

	public ReportTemplateFactory(AppContext c) {
		this(c, "ReportTemplate");
	}
	public ReportTemplateFactory(AppContext c,String table) {
		setContext(c, table);
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
				conn.error(e,"Bad template in ReportTemplate");
				throw new ValidateException("Cannot read template",e);
			} catch (ParserConfigurationException e) {
				conn.error(e,"Bad template in ReportTemplate");
				throw new ValidateException("XML Parser fault",e);
			} catch (InvalidArgument e) {
				conn.error(e,"Bad template in ReportTemplate");
				throw new ValidateException("Not found",e);
			} catch (TransformerFactoryConfigurationError e) {
				conn.error(e,"Bad template in ReportTemplate");
				throw new ValidateException("Bad template",e);
			} catch (TransformerException e) {
				conn.error(e,"Bad template in ReportTemplate");
				throw new ValidateException("Bad template",e);
			}
		}

		public TemplateNameInput() {
			super(false);
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
	
}
