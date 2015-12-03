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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.editors.xml.DomVisitor;
import uk.ac.ed.epcc.webapp.forms.inputs.ConstantInput;
import uk.ac.ed.epcc.webapp.jdbc.table.IntegerFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.xml.XMLOverlay;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** A TextFileOverlay that validates updates as report templates.
 * 
 * @author spb
 * @param <X> 
 *
 */


public class TemplateOverlay<X extends XMLOverlay.XMLFile> extends XMLOverlay<X> {

	public static class ReportFile extends XMLFile{

		protected ReportFile(Record r, URL base) {
			super(r, base);
			
		}
		public void incCounter(){
			if( record.getRepository().hasField(USE_COUNTER)){
				int count = record.getIntProperty(USE_COUNTER,0);
				count++;
				record.setProperty(USE_COUNTER, count);
				try {
					commit();
				} catch (DataFault e) {
					getContext().error(e,"Error increasing counter");
				}
			}
		}
	}
	private static final String USE_COUNTER = "UseCounter";
	private ReportBuilder builder=null;
	public TemplateOverlay(AppContext c, String table) throws URISyntaxException, ParserConfigurationException {
		super(c, table);
		
	}
	
	public ReportBuilder getBuilder() {
		if( builder == null){
			try {
				builder=new ReportBuilder(getContext());
			} catch (Exception e) {
				getContext().error(e,"Error making builder");
			}
		}
		return builder;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.DataObjectFactory#getSelectors()
	 */
	@Override
	protected Map<String, Object> getSelectors() {
		Map<String,Object> sel = new HashMap<String,Object>(super.getSelectors());
		ConstantInput<String> group_input = new ConstantInput<String>(ReportBuilder.REPORT_TEMPLATE_GROUP);
		group_input.setValue(ReportBuilder.REPORT_TEMPLATE_GROUP);
		sel.put(TextFileOverlay.GROUP, group_input);
		
		return sel;
	}

	@Override
	public Schema getSchema() {
		try {
			return getBuilder().getSchema(getContext().getInitParameter(ReportBuilder.REPORT_SCHEMA_CONFIG, ReportBuilder.DEFAULT_REPORT_SCHEMA));
		} catch (Exception e) {
			getContext().error(e,"Error getting schema");
			return null;
		}
	}

	@Override
	public boolean canView(SessionService<?> sess) {
		return sess.hasRoleFromList(SessionService.ADMIN_ROLE,ReportBuilder.REPORT_DEVELOPER);
	}

	@Override
	protected String getInitialCreateText() {
		try {
			return TextFileOverlay.getStringFromStream(getContext(),
					TextFileOverlay.getResourceStream(getContext(), ReportBuilder.REPORT_TEMPLATE_GROUP, "InitialReport.xml"));
		} catch (IOException e) {
			getContext().error(e,"Error making initial report");
			return null;
		}
	}

	@Override
	public DomVisitor getValidatingVisitor() {
		ReportBuilder b = getBuilder();
		try {
			// its actually the extensions with do the validating.
			b.setupExtensions(new HashMap<String, Object>());
		} catch (ParserConfigurationException e) {
			getContext().error(e,"Error setting up validator");
			return null;
		}
		return new ValidatingDomVisitor(b);
	}

	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c,
			String table) {
		final TableSpecification spec = super.getDefaultTableSpecification(c, table);
		spec.setOptionalField(USE_COUNTER, new IntegerFieldType());
		return spec;
	}
	
	
}