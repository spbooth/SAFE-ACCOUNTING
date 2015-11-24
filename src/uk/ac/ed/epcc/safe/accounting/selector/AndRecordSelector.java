// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.selector;


/** A class that encodes a selection expression for a set of UsageRecords.
 * 
 * Essentially this is an ordered set of SelectClause objects. We use an ordered set so that
 * different instances of the same selector don't randomly permute the order of the clauses in SQL 
 * statements improving the chance of query caching working.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: AndRecordSelector.java,v 1.4 2014/09/15 14:32:29 spb Exp $")

public class AndRecordSelector extends CombiningRecordSelector {
	
	
	public AndRecordSelector() {
		super("and");
	}

	public AndRecordSelector(RecordSelector sel){
		super("and" ,sel);
	}

	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		lock();
		return visitor.visitAndRecordSelector(this);
	}

	public AndRecordSelector copy() {
		if( isLocked()){
			return this;
		}
		AndRecordSelector copy = new AndRecordSelector(this);
		copy.lock();
		return copy;
	}

	


	

	
}