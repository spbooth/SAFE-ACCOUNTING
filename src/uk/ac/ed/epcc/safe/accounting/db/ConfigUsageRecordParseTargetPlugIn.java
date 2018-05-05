package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.update.ConfigPlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.webapp.AppContext;

public class ConfigUsageRecordParseTargetPlugIn<T extends UsageRecordFactory.Use, R> extends UsageRecordParseTargetPlugIn<T, R> {
	public ConfigUsageRecordParseTargetPlugIn(UsageRecordFactory<T> fac) {
		super(fac);
	}

	@Override
	protected PlugInOwner<R> makePlugInOwner(AppContext c, PropertyFinder finder, String table) {
		// For accounting record tables default to no parser
		// This will supress auto-table generation for unconfigured tables.
		// This is important as we may try to construct this class based on
		// a user input tag and we don't want to auto-create randomly named tables.
		return new ConfigPlugInOwner<ConfigUsageRecordFactory<T, R>,R>(c, finder,table);
	}

}
