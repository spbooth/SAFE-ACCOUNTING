// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.expr.TableMaker;

public abstract class ReportTableMaker<O extends UsageRecordFactory.Use> extends TableMaker<O> {

	public ReportTableMaker(AppContext c, Class<? super O> target) {
		super(c,target);
	}

}