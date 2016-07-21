package uk.ac.ed.epcc.safe.accounting.db;

import java.util.regex.Pattern;

import uk.ac.ed.epcc.webapp.model.data.Repository.Record;

public class RegexpTarget extends DataObjectPropertyContainer {

	@Override
	public String getIdentifier(int max_length) {
		return getName();
	}

	public RegexpTarget(RegexpTargetFactory fac, Record r) {
		super(fac, r);
	}
	
	public Pattern getRegexp(){
		return Pattern.compile(record.getStringProperty(RegexpTargetFactory.REGEX_FIELD));
	}

	public String getName(){
		return record.getStringProperty(RegexpTargetFactory.NAME_FIELD);
	}
}