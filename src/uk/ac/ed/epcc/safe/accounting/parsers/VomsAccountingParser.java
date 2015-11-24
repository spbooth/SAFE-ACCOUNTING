// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.webapp.AppContext;
@uk.ac.ed.epcc.webapp.Version("$Id: VomsAccountingParser.java,v 1.9 2014/09/15 14:32:25 spb Exp $")


public class VomsAccountingParser extends RegexpParser {
	public static final PropertyRegistry voms_reg = new PropertyRegistry("voms", "Properties from the voms_account log");
	@AutoTable(target=String.class,length=128)
	@Regexp("dn:\"([^\"]*)\"")
	public static final PropertyTag<String> VOMS_DN=new PropertyTag<String>(voms_reg, "Dn",String.class);
	@AutoTable(target=String.class,length=128,unique=true)
	@Regexp("jid:(\\d+-\\d+-\\d+\\.\\d+:\\d+:\\d+\\.\\d+\\.\\d+)")
	public static final PropertyTag<String> VOMS_ID=new PropertyTag<String>(voms_reg, "Id",String.class);
	@AutoTable(target=String.class)
	@Regexp("jid:\\d+-\\d+-\\d+\\.\\d+:\\d+:\\d+\\.\\d+\\.\\d+\\.([^ ]*)")
	public static final PropertyTag<String> VOMS_SUBMISSION=new PropertyTag<String>(voms_reg, "Submission",String.class);
	@Regexp("uid:(\\d+)")
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> VOMS_UID = new PropertyTag<Integer>(voms_reg, "UID", Integer.class);
    @AutoTable(target=String.class,length=128)
	@Regexp("vo0:\"([^\"]*)\"")
	public static final PropertyTag<String> VOMS_VO0 = new PropertyTag<String>(voms_reg, "VO0",String.class);
	@Regexp("poolindex:\"([^\"]*)\"")
	public static final PropertyTag<String> VOMS_POOLINDEX = new PropertyTag<String>(voms_reg, "PoolIndex",String.class);
	
	static{
		voms_reg.lock();
	}
	private AppContext conn;
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		this.conn=ctx;
		return voms_reg;
	}
	@Override
	public AppContext getContext() {
		return conn;
	}

}