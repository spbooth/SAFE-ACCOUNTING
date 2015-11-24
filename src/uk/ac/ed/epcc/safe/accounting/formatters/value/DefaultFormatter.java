// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.formatters.value;

/**
 * A simple default formatting object. This formatter will format any type of
 * object. All it does is call the object's <code>toString</code> method.
 * 
 * @author jgreen4
 * 
 */
@uk.ac.ed.epcc.webapp.Version("$Id: DefaultFormatter.java,v 1.6 2014/09/15 14:32:23 spb Exp $")

public class DefaultFormatter implements ValueFormatter<Object> {

	/**
	 * Useful static instance of this object to be used when one doesn't want to
	 * generate lots of formatters when one will suffice
	 */
	public static final DefaultFormatter FORMATTER = new DefaultFormatter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter#format(java
	 * .lang.Object)
	 */
	public String format(Object object) {
		return object.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter#getType()
	 */
	public Class<Object> getType() {
		return Object.class;
	}

}