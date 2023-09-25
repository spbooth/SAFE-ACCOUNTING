package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.*;

import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.inputs.InputVisitor;
import uk.ac.ed.epcc.webapp.forms.inputs.ListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TypeError;
import uk.ac.ed.epcc.webapp.forms.inputs.TypeException;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.session.SessionService;

public class ReportTypeRegistry extends AbstractContexed {
	public static final String REPORT_TYPE_PARAM = "ReportType";
	protected ReportTypeRegistry(AppContext conn) {
		super(conn);
		parseReportTypes();
	}

	private Map<String,ReportType> report_type_reg = new LinkedHashMap<>();
	// Standard ReportTypes These can be extended from the config
  //  public static final ReportType	HTML = new ReportType("HTML","html", "text/html","HTML web page"); 
  //  public static final ReportType	EHTML = new DeveloperReportType("EHTML","html","application/xhtml+xml","embedded XHTML web page");
//    public static final ReportType	PDF = new PDFReportType("PDF","pdf", "application/pdf", "Portable Document Format"); 
//	public static final ReportType FOP = new DeveloperReportType("FOP","fop","text/xml","FOP formating lanuage XML"); 
//    public static final ReportType	CSV = new CSVReportType("CSV","csv", "text/csv", "Comma seperated values");
//    public static final ReportType	XML = new DeveloperReportType("XML","xml", "text/xml","XML"); 
//    public static final ReportType	RXML = new DeveloperReportType("RXML","rxml", "text/xml","Raw XML before final formatting");
//    public static final ReportType	XHTML = new DeveloperReportType("XHTML","xhtml", "application/xhtml+xml","XHTML web page");
//    
//    // This one might be better in the config
//    public static final ReportType OGFXML = new DeveloperReportType("OGFXML","ogfxml", "text/xml","OGF accounting records");
//    public static final ReportType	TXT = new ReportType("TXT","txt", "text/plain", "Plain text output");
	public class ReportTypeInput extends TextInput implements ListInput<String, ReportType>{

		public ReportType getItem() {
			return report_type_reg.get(getValue());
		}

		@Override
		public String getValueByItem(ReportType item) {
			return item.toString();
		}

		public ReportType getItembyValue(String value) {
			return report_type_reg.get(value);
		}

		public Iterator<ReportType> getItems() {
			Set<ReportType> items = new LinkedHashSet<>();
			SessionService user = getContext().getService(SessionService.class);
			for(ReportType t : report_type_reg.values()){
				if( t.allowSelect(user)){
					items.add(t);
				}
			}
			return items.iterator();
		}
		public int getCount() {
			int count=0;
			SessionService user = getContext().getService(SessionService.class);
			for(ReportType t : report_type_reg.values()){
				if( t.allowSelect(user)){
					count++;
				}
			}
			return count;
		}

		public String getTagByItem(ReportType item) {
			return item.toString();
		}

		public String getTagByValue(String value) {
			return value;
		}

		public String getText(ReportType item) {
			return item.description;
		}


		@Override
		public boolean isValid(ReportType item) {
			return report_type_reg.containsValue(item);
		}

		@Override
		public ReportType getItemByTag(String tag) {
			return getItembyValue(tag);
		}

		@Override
		public String getValueByTag(String tag) {
			return tag;
		}
		
	}
	protected ReportType getReportType(Map<String, Object> params) {
		ReportType type = null;

		// See if there's a param
		Object reportTypeParam = params.get(REPORT_TYPE_PARAM);
		if (reportTypeParam instanceof ReportType) {
			return (ReportType) reportTypeParam;

		}else if( reportTypeParam instanceof String){
			type = report_type_reg.get(reportTypeParam.toString());
		}

		if (type == null) {
			type = getReportType("HTML");
		}
		return type;
	}
	public static final String REPORT_TYPE_CONFIG_PREFIX="report_type";
	
	/** Get sub-classed {@link ReportType}s that are not declared via Config.
	 * 
	 * @return
	 */
	protected Set<ReportType> getSpecialReportTypes(){
		LinkedHashSet<ReportType> special = new LinkedHashSet<>();
//		special.add(HTML);
//		special.add(EHTML);
		return special;
	}
	
	public Collection<ReportType> getReportTypes(){
		return  Collections.unmodifiableCollection(report_type_reg.values());
	}
	/** Look up a report type.
	 * If we can't find by name try looking up by extension.
	 * 
	 * @param text
	 * @return ReportType
	 */
	public ReportType getReportType(String text) {
		ReportType type = report_type_reg.get(text);
		if( type != null){
			return type;
		}
		for(ReportType t : report_type_reg.values()){
			if( t.getExtension().equalsIgnoreCase(text)){
				return t;
			}
		}
		return null;
	}
	public ReportType getTemplateType(String templateFileName) {

		int dotLocation = templateFileName.lastIndexOf('.');
		if (dotLocation > 0) {
			String extension = templateFileName.substring(dotLocation + 1,
					templateFileName.length());
			return getReportType(extension);

		} else {
			return null;

		}

	}
	private final void parseReportTypes(){
		for(ReportType t : getSpecialReportTypes()){
			report_type_reg.put(t.name(), t);
		}
		
		
		AppContext conn = getContext();		
		Logger log = conn.getService(LoggerService.class).getLogger(getClass());
		
		String list = conn.getInitParameter(REPORT_TYPE_CONFIG_PREFIX+".list", "");
		for(String name : list.split("\\s*,\\s*")){
			String extension = conn.getInitParameter(REPORT_TYPE_CONFIG_PREFIX+"."+name+".extension", name.toLowerCase());
			String mime = conn.getInitParameter(REPORT_TYPE_CONFIG_PREFIX+"."+name+".mime", "text/"+name.toLowerCase());
			String description = conn.getInitParameter(REPORT_TYPE_CONFIG_PREFIX+"."+name+".description", name.toLowerCase());
			String help = conn.getInitParameter(REPORT_TYPE_CONFIG_PREFIX+"."+name+".help");
			String image = conn.getInitParameter(REPORT_TYPE_CONFIG_PREFIX+"."+name+".image");
			Class<? extends ReportType> clazz = conn.getPropertyClass(ReportType.class, ReportType.class, name);
			try {
				ReportType type= conn.makeParamObject(clazz, name,extension,mime,description,help,image);
				if( type != null  ){
					report_type_reg.put(name,type);
				}
			} catch (Exception e) {
				log.debug("Error making report type "+name,e);
			}
			
		}
	}
	private static final String REG_ATTR="ReportTypeRegistryAttr";
	public static ReportTypeRegistry getInstance(AppContext conn) {
		ReportTypeRegistry reg = (ReportTypeRegistry) conn.getAttribute(REG_ATTR);
		if( reg == null) {
			reg = new ReportTypeRegistry(conn);
			conn.setAttribute(REG_ATTR, reg);
		}
		return reg;
	}
}
