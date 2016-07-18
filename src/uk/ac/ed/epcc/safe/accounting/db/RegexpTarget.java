package uk.ac.ed.epcc.safe.accounting.db;

import java.util.regex.Pattern;

import uk.ac.ed.epcc.webapp.model.data.Repository.Record;

public class RegexpTarget extends DataObjectPropertyContainer {

	public RegexpTarget(RegexpTargetFactory fac, Record r) {
		super(fac, r);
	}
	
	public Pattern getRegexp(){
		return Pattern.compile(record.getStringProperty(RegexpTargetFactory.REGEX_FIELD));
	}

}
