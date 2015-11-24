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
@uk.ac.ed.epcc.webapp.Version("$Id: OrRecordSelector.java,v 1.4 2014/09/15 14:32:29 spb Exp $")

public class OrRecordSelector extends CombiningRecordSelector {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1542634443085947726L;

	public OrRecordSelector() {
		super("or");
	}

	public OrRecordSelector(RecordSelector c) {
		super("or",c);
	}

	public <R> R visit(SelectorVisitor<R> visitor) throws Exception {
		lock();
		return visitor.visitOrRecordSelector(this);
	}

	public OrRecordSelector copy() {
		if( isLocked() ){
			return this;
		}
		OrRecordSelector copy = new OrRecordSelector(this);
		copy.lock();
		return copy;
	}

	

	
}