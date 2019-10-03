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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.CurrentTimeService;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.ExtendedXMLBuilder;
import uk.ac.ed.epcc.webapp.content.Link;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.editors.xml.DomVisitor;
import uk.ac.ed.epcc.webapp.forms.html.RedirectResult;
import uk.ac.ed.epcc.webapp.forms.inputs.ConstantInput;
import uk.ac.ed.epcc.webapp.jdbc.table.DateFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.IntegerFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.ReferenceFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.forms.registry.SummaryContentProvider;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedTypeProducer;
import uk.ac.ed.epcc.webapp.model.xml.XMLOverlay;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.AppUserFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** A TextFileOverlay that validates updates as report templates.
 * 
 * @author spb
 * @param <X> 
 *
 */


public class TemplateOverlay<X extends TemplateOverlay.ReportFile> extends XMLOverlay<X> implements SummaryContentProvider<X>{

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
					getLogger().error("Error increasing counter",e);
				}
			}
		}
		public Date getLastModified() {
			return record.getDateProperty(UPDATED);
		}
		public void setLastModified(Date d) {
			record.setOptionalProperty(UPDATED, d);
		}
		public <A extends AppUser> A getLastEditor() {
			@SuppressWarnings("unchecked")
			AppUserFactory<A> loginFactory = getContext().getService(SessionService.class).getLoginFactory();
			return record.getProperty(new IndexedTypeProducer<>(getContext(), UPDATED_BY, loginFactory));
		}
		public <A extends AppUser> void setLastEditor(A person) {
			@SuppressWarnings("unchecked")
			AppUserFactory<A> loginFactory = getContext().getService(SessionService.class).getLoginFactory();
			record.setOptionalProperty(new IndexedTypeProducer<>(getContext(), UPDATED_BY, loginFactory),person);
		}
		@Override
		protected void pre_commit(boolean dirty) throws DataFault {
			if( dirty) {
				try {
					setLastModified( getContext().getService(CurrentTimeService.class).getCurrentTime());
					setLastEditor(getContext().getService(SessionService.class).getCurrentPerson());
				}catch(Exception t) {
					getLogger().error("Error logging change", t);
				}
			}
		}
	}
	private static final String USE_COUNTER = "UseCounter";
	private static final String UPDATED_BY = "UpdatedBy";
	private static final String UPDATED = "Updated";
	private ReportBuilder builder=null;
	public TemplateOverlay(AppContext c, String table) throws URISyntaxException, ParserConfigurationException {
		super(c, table);
		
	}
	
	public ReportBuilder getBuilder() {
		if( builder == null){
			try {
				builder=new ReportBuilder(getContext());
			} catch (Exception e) {
				getLogger().error("Error making builder",e);
			}
		}
		return builder;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.DataObjectFactory#getSelectors()
	 */
	@Override
	protected Map<String, Object> getSelectors() {
		Map<String,Object> sel = new HashMap<>(super.getSelectors());
		ConstantInput<String> group_input = new ConstantInput<>(ReportBuilder.REPORT_TEMPLATE_GROUP);
		group_input.setValue(ReportBuilder.REPORT_TEMPLATE_GROUP);
		sel.put(TextFileOverlay.GROUP, group_input);
		
		return sel;
	}

	@Override
	public Schema getSchema() {
		try {
			return getBuilder().getSchema(getContext().getInitParameter(ReportBuilder.REPORT_SCHEMA_CONFIG, ReportBuilder.DEFAULT_REPORT_SCHEMA));
		} catch (Exception e) {
			getLogger().error("Error getting schema",e);
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
			getLogger().error("Error making initial report",e);
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
			getLogger().error("Error setting up validator",e);
			return null;
		}
		return new ValidatingDomVisitor(b);
	}

	@Override
	protected TableSpecification getDefaultTableSpecification(AppContext c,
			String table) {
		final TableSpecification spec = super.getDefaultTableSpecification(c, table);
		spec.setOptionalField(USE_COUNTER, new IntegerFieldType());
		spec.setOptionalField(UPDATED, new DateFieldType(true, null));
		spec.setOptionalField(UPDATED_BY, new ReferenceFieldType(c.getService(SessionService.class).getLoginFactory().getTag()));
		return spec;
	}

	@Override
	public <C extends ContentBuilder> C getSummaryContent(AppContext c, C cb, X target) {
		String schemas = c.getInitParameter("schema.links");
		if( schemas != null && cb instanceof ExtendedXMLBuilder) {
			cb.addHeading(4, "Schema documentation");
			Set<Link> links = new LinkedHashSet<>();
			for(String name : schemas.split("\\s*,\\s*")) {
				links.add(new Link(c, name, new RedirectResult("/templates/schema/"+name)));
			}
			cb.addList(links);
		}
		try {
			Table t = new Table();
			Date lastModified = target.getLastModified();
			if( lastModified != null) {
				t.put("Value", "Last updated", lastModified);
			}
			AppUser lastEditor = target.getLastEditor();
			if( lastEditor != null) {
				t.put("Value", "Updated by", lastEditor);
			}
			t.setKeyName("Property");
			if( t.hasData()) {
				cb.addColumn(c, t, "Value");
			}
		}catch(Exception t) {
			getLogger().error("Error adding edit info",t);
		}
		return cb;
	}

	@Override
	protected Set<String> getSupress() {
		Set<String> supress = super.getSupress();
		supress.add(USE_COUNTER);
		supress.add(UPDATED);
		supress.add(UPDATED_BY);
		return supress;
	}

	@Override
	protected X makeBDO(Record res) throws DataFault {
		return (X) new ReportFile(res, getBaseURL());
	}

	@Override
	public Class<X> getTarget() {
		return (Class<X>) ReportFile.class;
	}
	
	
}