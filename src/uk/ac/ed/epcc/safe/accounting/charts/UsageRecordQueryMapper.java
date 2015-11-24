// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.Vector;

import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.webapp.charts.strategy.KeyLabeller;
import uk.ac.ed.epcc.webapp.charts.strategy.LabelledQueryMapper;

public abstract class UsageRecordQueryMapper<K> implements LabelledQueryMapper<UsageProducer<?>> {

	
	final protected KeyLabeller<UsageRecord,K> labeller;


	public UsageRecordQueryMapper(
			KeyLabeller<UsageRecord,K> lab){
		labeller=lab;
	}
	
	public Vector<String> getLabels() {
		return labeller.getLabels();
	}

	public int nSets() {
		return labeller.nSets();
	}

	public int getSet(UsageRecord o) {
		return labeller.getSet(o);
	}
	


}