package uk.ac.ed.epcc.safe.accounting.model;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyContainer;
import uk.ac.ed.epcc.safe.accounting.db.DefaultDataObjectPropertyFactory;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.CurrentTimeService;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.table.DateFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.ReferenceFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.StringFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.AppUserFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;

public class ReportTemplateLog extends DataObjectPropertyContainer implements AutoCloseable {
    
    public ReportTemplateLog(DataObjectFactory<?> fac, Record r) {
        super(fac, r);
    }

    public static class ReportLogFactory extends DefaultDataObjectPropertyFactory<ReportTemplateLog> {

    	 private static final String TIMESTAMP = "Timestamp";    
         private static final String FINISH = "FinishTime";
         private static final String PERSON_ID = "PersonID";
         private static final String REPORT_TEMPLATE_ID = "ReportTemplateID";
         private static final String PARAMETERS = "Parameters";
		public static final PropertyRegistry reportlog_reg = new PropertyRegistry("reportlog", "report log properties");
        public static final ReferenceTag<AppUser, AppUserFactory> person_tag = 
        		new ReferenceTag<>(reportlog_reg, "Person",  AppUserFactory.class, "Person");
        public static final PropertyTag<Duration> runtime = new PropertyTag<Duration>(reportlog_reg,"Runtime",Duration.class,"time report took to run");
        static {
        	reportlog_reg.lock();
        }
        public static final String DEFAULT_TABLE = "ReportTemplateLog";

       
        private AppUserFactory<?> userFac;
        private ReportTemplateFactory<?> templateFac;

        public ReportLogFactory(AppContext ctx) {
            this(ctx, DEFAULT_TABLE, null, null);
        }

        public ReportLogFactory(AppContext ctx, String table) {
            this(ctx, table, null, null);
        }

        public ReportLogFactory(AppContext ctx, String table, AppUserFactory<?> uf, ReportTemplateFactory<?> tf) {
            this.setContext(ctx, table);
            userFac = uf;
            templateFac = tf;
        }
        
        public ReportLogFactory(AppContext ctx, AppUserFactory<?> uf, ReportTemplateFactory<?> tf) {
            this(ctx, DEFAULT_TABLE, uf, tf);
        }
        
        @Override
        public TableSpecification getDefaultTableSpecification(AppContext ctx,
                 String homeTable) {
            userFac = ctx.getService(SessionService.class).getLoginFactory();
            templateFac = new ReportTemplateFactory<>(ctx);
            TableSpecification spec = new TableSpecification();
            spec.setField(TIMESTAMP, new DateFieldType(true, null));
            spec.setField(FINISH, new DateFieldType(true, null)); // can cope without this
            spec.setOptionalField(PERSON_ID, new ReferenceFieldType(userFac.getTag()));
            spec.setField(REPORT_TEMPLATE_ID, new ReferenceFieldType(templateFac.getTag()));
            spec.setField(PARAMETERS, new StringFieldType(true, null, 1000));
           
            return spec;
        }

        @Override
        protected ReportTemplateLog makeBDO(Record res) throws DataFault {
            return new ReportTemplateLog(this, res);
        }

        @Override
        public void customAccessors(
                AccessorMap<ReportTemplateLog> mapi,
                MultiFinder finder,
                PropExpressionMap derived) {
            super.customAccessors(mapi, finder, derived);
            finder.addFinder(reportlog_reg);
            finder.addFinder(StandardProperties.time);
            mapi.put(StandardProperties.STARTED_PROP, res.getDateExpression(getTarget(), TIMESTAMP));
            mapi.put(StandardProperties.ENDED_PROP, res.getDateExpression(getTarget(), FINISH));
            PropExpression<Duration> expr = new DurationPropExpression(StandardProperties.STARTED_PROP, StandardProperties.ENDED_PROP);
			try {
				derived.put(runtime, expr);
			} catch (PropertyCastException e) {
				getLogger().error("Error setting derived props",e);
			}
        }

        public ReportTemplateLog logReport(AppUser user, ReportTemplate template, List<String> parameters) throws DataFault {
            ReportTemplateLog log = makeBDO();
            CurrentTimeService time = getContext().getService(CurrentTimeService.class);
            log.record.setProperty(TIMESTAMP, time.getCurrentTime());
            log.record.setProperty(REPORT_TEMPLATE_ID, template);
            if (user != null) {
                log.record.setOptionalProperty(PERSON_ID, user.getID());
            }
            if (parameters != null) {
            	// requires Java-8
                //String param_string = String.join("/", parameters);
            	StringBuilder sb = new StringBuilder();
            	for( String s : parameters) {
            		if( sb.length() > 0 ) {
            			sb.append("/");
            		}
            		sb.append(s);
            	}
				log.record.setProperty(PARAMETERS, sb.toString());
            }
            log.commit();
            return log;
        }

    }

    public ReportTemplate getTemplate() {
        return (ReportTemplate) record.getProperty(ReportLogFactory.REPORT_TEMPLATE_ID);
    }

    public AppUser getPerson() throws DataException {
    	if( ! recordPerson()) {
    		return null;
    	}
    	AppUserFactory login = getContext().getService(SessionService.class).getLoginFactory();
        return (AppUser) login.find(record.getNumberProperty(ReportLogFactory.PERSON_ID));
    }

	/**
	 * @return
	 */
	public boolean recordPerson() {
		return record.getRepository().hasField(ReportLogFactory.PERSON_ID);
	}

	/** record and commit report runtime.
	 * 
	 * @
	 * @throws DataFault
	 */
	public void recordFinish()  {
		try {
			CurrentTimeService time = getContext().getService(CurrentTimeService.class);
			record.setOptionalProperty(ReportLogFactory.FINISH, time.getCurrentTime());
			commit();
		}catch(Exception e){
			getLogger().error("Error recording finish", e);
		}
	}
    public String getParameters() {
        return record.getStringProperty(ReportLogFactory.PARAMETERS);
    }

    public List<String> getParametersList() {
        String parameters = record.getStringProperty(ReportLogFactory.PARAMETERS);
        if (parameters == null) {
            return null;
        }
        if (parameters.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(parameters.split("/"));
    }

	@Override
	public void close() throws Exception {
		recordFinish();
		
	}

}
