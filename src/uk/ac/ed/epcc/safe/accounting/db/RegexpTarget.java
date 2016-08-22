package uk.ac.ed.epcc.safe.accounting.db;

import java.util.regex.Pattern;

import uk.ac.ed.epcc.webapp.model.data.Repository.Record;

public class RegexpTarget extends AccountingClassification {

	

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

}
