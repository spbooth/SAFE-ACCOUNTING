package uk.ac.ed.epcc.safe.accounting.model;

import java.util.Date;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.db.NameFinderUploadParseTargetPlugIn;
import uk.ac.ed.epcc.safe.accounting.db.UploadParseTargetPlugIn;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.AppUserFactory;
import uk.ac.ed.epcc.webapp.session.EmailNameFinder;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.session.SignupDateComposite;
import uk.ac.ed.epcc.webapp.session.WebNameFinder;
/** A {@link UploadParseTargetPlugIn} for {@link AppUserFactory} classes
 * 
 * If configuration tag <em>config-tag</em><b>.match</b> is set this is the name of 
 * a property in the parse to match records on. This defaults to <b>WebName</b>.
 * This is applied to the default realm of the {@link AppUserFactory}.
 * If the feature <b>person.make_on_upload<b> is the parser will attempt to create missing
 * people on upload.
 * @author spb
 * @param <A> type of {@link AppUser}
 * @param <R> intermediate type of parser
 *
 */
public class AppUserUploadParseTargetPlugin<A extends AppUser,R> extends NameFinderUploadParseTargetPlugIn<A,R> {
	public static final Feature MAKE_ON_UPLOAD_FEATURE = new Feature("person.make_on_upload",true,"On a person upload unknown users will be created as well as existing ones updated");
	
	private static final PropertyRegistry person_registy = new PropertyRegistry("appuser","Properties associated with the Person class");
    public static final PropertyTag<String> WEBNAME_PROP = new PropertyTag<String>(person_registy,WebNameFinder.WEB_NAME,String.class,"Web authenticated REMOTE_USER name");
    public static final PropertyTag<String> EMAIL_PROP = new PropertyTag<String>(person_registy,EmailNameFinder.EMAIL,String.class,"Users Email address");
    public static final PropertyTag<Date> SIGNUP_PROP = new PropertyTag<Date>(person_registy,SignupDateComposite.SIGNUP_DATE,Date.class,"First access to system");
	
    
	
	public AppUserUploadParseTargetPlugin(AppUserFactory<A> fac) {
		super(fac);
	}

	/** Control if new records should be made when seen in an parse 
	 * 
	 * @return
	 */
	protected final boolean makeOnUpload(){
		return MAKE_ON_UPLOAD_FEATURE.isEnabled(getContext());
	}
	

	@Override
	public void customAccessors(AccessorMap<A> mapi2, MultiFinder finder, PropExpressionMap derived) {
		finder.addFinder(person_registy);
		Set<String> role_list = getContext().getService(SessionService.class).getStandardRoles();
		if( role_list != null && !role_list.isEmpty()){
			@SuppressWarnings("unchecked")
			SessionService<A> serv = getContext().getService(SessionService.class);

			PropertyRegistry role_reg = new PropertyRegistry("roles", "role properties");
			for( String role :  role_list){
				try {
					PropertyTag<Boolean> role_tag = new PropertyTag<Boolean>(role_reg, role, Boolean.class);
					mapi2.put(role_tag, new RoleAccessor<A>(serv, role));
				}catch(Throwable t) {
					getLogger().error("Error making role accessor for ["+role+"]", t);
				}
			}
			finder.addFinder(role_reg);
		}
		// initialise the plug-in
		super.customAccessors(mapi2, finder, derived);
		
	}

	@Override
	protected String getDefaultMatchPropName() {
		if( getRepository().hasField(WebNameFinder.WEB_NAME)) {
			return WebNameFinder.WEB_NAME;
		}
		return super.getDefaultMatchPropName();
	}
	
	

}
