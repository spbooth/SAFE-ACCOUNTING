package uk.ac.ed.epcc.safe.accounting.db;

import java.util.regex.Pattern;

import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.Matcher;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;

public class RegexpTarget extends AccountingClassification implements Matcher {

	

	public RegexpTarget(RegexpTargetFactory fac, Record r) {
		super(fac, r);
	}
	
	private Pattern patt=null;
	public Pattern getRegexp(){
		if( patt == null ){
			patt = Pattern.compile(record.getStringProperty(RegexpTargetFactory.REGEX_FIELD));
		}
		return patt;
	}
	public void setRegexp(Pattern p) {
		record.setOptionalProperty(RegexpTargetFactory.REGEX_FIELD, p.pattern());
	}
	@Override
	public boolean matches(String name) {
		return getRegexp().matcher(name).matches();
	}

}
