// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.ExtendedXMLBuilder;
import uk.ac.ed.epcc.webapp.logging.Logger;

/**
 * Collection of error messages.
 * 
 * Where it is not possible to report errors as they occur, For example when
 * parsing accounting data. An ErrorSet can be used to store all errors for
 * later reporting. Each error is recorded as a general type and specific
 * details. This allows the class to generate a summary report giving just the
 * numbers of errors of each general class.
 * 
 * @author spb
 * 
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ErrorSet.java,v 1.18 2014/10/06 16:02:27 spb Exp $")

public class ErrorSet
{
  public static class Detail
  {
    public final String text;
    public final Throwable t;

    public Detail(String text, Throwable t)
    {
      this.text = text;
      this.t = t;
    }

    /**
     * @return The data that caused the error. The string may be badly formatted
     *         or contain information that is inconsistent with itself.
     */
    public String getText()
    {
      return this.text;
    }

    /**
     * @return the exception that announced the error. May return
     *         <code>null</code> if the exception that caused the error was not
     *         specified.
     */
    public Throwable getThrowable()
    {
      return this.t;
    }
  }

  public static class Entry implements Comparable<Entry>
  {
    private int count = 0;
    String text;

    public Entry(String text)
    {
      this.text = text;
    }

    Set<Detail> fails = new HashSet<Detail>();

    public void add(String value, Throwable t)
    {
      if(value != null && value.length() > 0)
      {
        count++;
        fails.add(new Detail(value, t));
      }
    }

    public int compareTo(Entry o)
    {
      if(count == o.count)
      {
        return text.compareTo(o.text);
      }
      return count - o.count;
    }

    public String getText()
    {
      return this.text;
    }

    public int getCount()
    {
      return this.count;
    }

    public Collection<Detail> getDetails()
    {
      return Collections.unmodifiableCollection(fails);
    }
  }
  private String name="";

  private java.util.Map<String, Entry> reg = new TreeMap<String, Entry>();

  public int size()
  {
    return reg.size();
  }

  public void setName(String name){
	  this.name=name;
  }
  /**
   * Add an error
   * 
   * @param text
   *          String general class or error
   * @param data
   *          String specific details of this error
   */
  public void add(String text, String data)
  {
    add(text, data, null);
  }

  public void add(String text, String data, Throwable t)
  {
    Entry e;
    if(reg.containsKey(text))
    {
      e = reg.get(text);
    } else
    {
      e = new Entry(text);
      reg.put(text, e);
    }
    e.add(data, t);
  }

  @Override
  public String toString()
  {
	  return details(10);
  }
  public String details()
  {
	  return details(-1);
  }
  /** Generate text precis of the errors
   * The data texts will only be printed if there are less than nprint values or
   * if nprint is -1.
   * @param nprint target number of lines 
   * @return String
   */
  public String details(int nprint)
  {
	  StringBuilder sb = new StringBuilder();
	  for(String k : reg.keySet())
	  {
		  Entry e = reg.get(k);
		  sb.append(e.count);
		  sb.append(": ");
		  sb.append(k);
		  sb.append('\n');
		  if( nprint < 0 || e.count < nprint){
			  for(Detail lines : e.fails)
			  {
				  sb.append("\t");
				  sb.append(lines.text);
				  sb.append("\n");
			  }
		  }
	  }
	  return sb.toString();
  }
  
  public <X extends ContentBuilder> X addContent(X cb, int nprint){
	  if(name != null && name.trim().length() > 0){
		  cb.addHeading(5,name);
	  }
	  for(String k : reg.keySet())
	  {
		  ExtendedXMLBuilder sb = cb.getText();
		  Entry e = reg.get(k);
		  sb.clean(e.count);
		  sb.clean(": ");
		  sb.clean(k);
		  sb.open("br");
		  sb.close();
		  sb.clean('\n');
		  if( nprint < 0 || e.count < nprint){
			  for(Detail lines : e.fails)
			  {
				  sb.nbs();
				  sb.clean(lines.text);
				  sb.open("br");
				  sb.close();
				  sb.clean('\n');
				  Throwable t = lines.getThrowable();
				  while(t != null){
					  String message = t.getMessage();
					  if( message != null ){
						  sb.nbs();
						  sb.nbs();
						  sb.clean(message);
						  sb.open("br");
						  sb.close();
						  sb.clean("\n");
					  }
					  t = t.getCause();
				  }
			  }
		  }
		  sb.appendParent();
	  }
	  return cb;
  }
  /**
   * @return All error entries that have been recorded by this
   *         <code>ErrorSet</code>
   */
  public Collection<Entry> getEntries()
  {
    return this.reg.values();
  }

  /**
   * Returns the details of all the errors that are stored in this
   * <code>ErrorSet</code>. <code>Detail</code> objects contain the data that
   * coursed the error and may contain the <code>Throwable</code> that
   * represents the error too. The number of times the error occured is not
   * recorded in a <code>Detail</code> object. For this kind of information, use
   * {@link #getEntries} instead.
   * 
   * @return details of all the different errors that are recorded in this
   *         <code>ErrorSet</code>
   */
  public Collection<Detail> getAllErrorDetails()
  {
    ArrayList<Detail> allDetails = new ArrayList<Detail>();
    for(Entry e : this.reg.values())
      for(Detail d : e.fails)
        allDetails.add(d);

    return allDetails;
  }

  public void traceback(PrintStream s)
  {
    for(String k : reg.keySet())
    {
      Entry e = reg.get(k);
      s.print(e.count);
      s.print(": ");
      s.println(k);
      for(Detail lines : e.fails)
      {
        s.print("\t");
        s.println(lines.text);
        if(lines.t != null)
        {
          lines.t.printStackTrace(s);
        }
      }
    }
    s.println("------");
  }
  public void report(AppContext c){
	  for(String k : reg.keySet())
	    {
	      Entry e = reg.get(k);
	      for(Detail lines : e.fails)
	      {
	        c.error(lines.getThrowable(),lines.getText());
	      }
	    }
	 
  }
  public void report(Logger l){
	  for(String k : reg.keySet())
	    {
	      Entry e = reg.get(k);
	      for(Detail lines : e.fails)
	      {
	        l.error(lines.getText(),lines.getThrowable());
	      }
	    }
	 
  }
}