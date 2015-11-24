// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.text.SimpleDateFormat;


/**
   * Parses a string into a <code>Date</code>. The string should be a date in
   * the format used by PBS records. That is : MM/dd/yyyy HH:mm:ss. For example:
   * 01/20/2001 21:23:52
   * 
   * @author jgreen4
   * 
   */
@uk.ac.ed.epcc.webapp.Version("$Id: PbsDateParser.java,v 1.5 2014/09/15 14:32:26 spb Exp $")

  public class PbsDateParser extends DateParser
  {
    /**
     * Useful static instance of this object to be used when one doesn't want to
     * generate lots of parsers when one will suffice
     */
    public static final PbsDateParser PARSER = new PbsDateParser();

    public PbsDateParser()
    {
      /**
       * The date format used by PBS records
       */
      super(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"));
    }
  }