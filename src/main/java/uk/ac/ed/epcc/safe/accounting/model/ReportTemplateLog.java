package uk.ac.ed.epcc.safe.accounting.model;


import java.util.*;

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
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.MessageResult;
import uk.ac.ed.epcc.webapp.forms.transition.DirectTransition;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.table.*;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.TableStructureContributer;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.filter.NullFieldFilter;
import uk.ac.ed.epcc.webapp.model.data.forms.Selector;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.AppUserFactory;
import uk.ac.ed.epcc.webapp.session.SessionService;

public class ReportTemplateLog extends DataObjectPropertyContainer implements AutoCloseable {
    
    public ReportTemplateLog(DataObjectFactory<?> fac, Record r) {
        super(fac, r);
    }

    public static class ReportLogFactory extends DefaultDataObjectPropertyFactory<ReportTemplateLog>  implements TableTransitionContributor{

    	 private static final String EXT = "ext";
		private static final String NAME = "Name";
		private static final String TIMESTAMP = "Timestamp";    
         private static final String FINISH = "FinishTime";
         private static final String PERSON_ID = "PersonID";
         private static final String REPORT_TEMPLATE_ID = "ReportTemplateID";
         private static final String PARAMETERS = "Parameters";
		public static final PropertyRegistry reportlog_reg = new PropertyRegistry("reportlog", "report log properties");
        public static final ReferenceTag<AppUser, AppUserFactory> person_tag = 
        		new ReferenceTag<>(reportlog_reg, "Person",  AppUserFactory.class, "Person");
        public static final PropertyTag<Duration> runtime = new PropertyTag<Duration>(reportlog_reg,"Runtime",Duration.class,"time report took to run");
        public static final PropertyTag<String> name = new PropertyTag<>(reportlog_reg, NAME, String.class,"Template file base-name");
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
            spec.setOptionalField(NAME, new StringFieldType(true,null,64));
            spec.setOptionalField(EXT, new StringFieldType(true,null,5));
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
            mapi.put(StandardProperties.STARTED_PROP, res.getDateExpression( TIMESTAMP));
            mapi.put(StandardProperties.ENDED_PROP, res.getDateExpression( FINISH));
            PropExpression<Duration> expr = new DurationPropExpression(StandardProperties.STARTED_PROP, StandardProperties.ENDED_PROP);
			try {
				derived.put(runtime, expr);
			} catch (PropertyCastException e) {
				getLogger().error("Error setting derived props",e);
			}
        }

        public ReportTemplateLog logReport(AppUser user, ReportTemplate template,String name,String ext, List<String> parameters) throws DataFault {
            ReportTemplateLog log = makeBDO();
            CurrentTimeService time = getContext().getService(CurrentTimeService.class);
            log.record.setProperty(TIMESTAMP, time.getCurrentTime());
            log.record.setProperty(REPORT_TEMPLATE_ID, template);
            log.record.setOptionalProperty(NAME, name);
            log.record.setOptionalProperty(EXT, ext);
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
        public void setTemplateFileName(ReportTemplateLog log,String name) throws DataFault {
        	if( name != null ) {
        		if( name.contains(".")) {
        			String parts[] = name.split("\\.");
        			if( parts.length == 2) {
        				log.record.setOptionalProperty(NAME, parts[0]);
        				log.record.setOptionalProperty(EXT, parts[1]);
        			}
        		}else {
        			log.record.setOptionalProperty(NAME, name);
        		}
        	}
        	log.commit();
        }

        public class FixupNames implements DirectTransition<ReportLogFactory>{

			@Override
			public FormResult doTransition(ReportLogFactory target, AppContext c) throws TransitionException {
				int count=0;
				if( res.hasField(NAME)) {
					try {
						for(ReportTemplateLog log : target.getResult(new NullFieldFilter<ReportTemplateLog>(res, NAME, true))) {
							List<String> param = log.getParametersList();
							if( param != null && ! param.isEmpty()) {
								String template = param.get(param.size()-1);
								setTemplateFileName(log, template);
								count++;
							}
						}
					} catch (DataFault e) {
						getLogger().error("Error finding entries to update", e);
					}
				}
				return new MessageResult("template_log_updated", count);
			}
        	
        }
		@Override
		public Map<TableTransitionKey, Transition> getTableTransitions() {
			 Map<TableTransitionKey, Transition> result = new LinkedHashMap<>();
			 if( res.hasField(NAME)) {
				 result.put(new AdminOperationKey("UpdateName", "Set missing template names from parameters"), new FixupNames());
			 }
			return result;
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
