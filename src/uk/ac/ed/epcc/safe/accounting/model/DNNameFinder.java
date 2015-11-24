package uk.ac.ed.epcc.safe.accounting.model;


import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.AppUserFactory;
import uk.ac.ed.epcc.webapp.session.AppUserNameFinder;
import uk.ac.ed.epcc.webapp.session.FieldNameFinder;
/** A {@link AppUserNameFinder} that stores DNs.
 * 
 * These can either be in LDAP or SSL form. The database always holds the SSL form.
 */
public class DNNameFinder<AU extends AppUser> extends FieldNameFinder<AU, DNNameFinder>{

	@Override
	public boolean userVisible() {
		return true;
	}
	public DNNameFinder(AppUserFactory factory, String realm) {
		super(factory, realm);
	}
	public static String toWebFormat(String dn){
		dn=dn.trim();
		if( dn.startsWith("/")){
			return dn;
		}
		StringBuilder sb = new StringBuilder();
		String fields[] = dn.split("\\s*,\\s*");
		for(int i = fields.length-1; i>= 0 ; i--){
			sb.append("/");
			sb.append(fields[i]);
		}
		return sb.toString();
	}
	public static String toLdapFormat(String dn){
		dn=dn.trim();
		if( dn.startsWith("/")){
			StringBuilder sb = new StringBuilder();
			String fields[] = dn.substring(1).split("\\s*/\\s*"); // omit leading slash
			for(int i = fields.length-1; i>= 0 ; i--){
				if( i != fields.length-1){
					sb.append(",");
				}
				sb.append(fields[i]);
			}
			return sb.toString();
		}else{
			return dn;
		}
	}
	@Override
	public String normalizeName(String name){
		return toWebFormat(name);
	}
}