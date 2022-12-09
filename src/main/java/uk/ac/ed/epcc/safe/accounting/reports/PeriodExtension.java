//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.safe.accounting.reports.exceptions.UnexpandedContentException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.time.*;


/** Extension for parsing time period specifications from the XML report.
 * 
 * 
 * @author spb
 *
 */
public class PeriodExtension extends ReportExtension {

	
	private static final String PERIOD = PERIOD_ELEMENT;

	/** static formatting method. 
	 * It lives here to keep the dependencies together.
	 * 
	 * @param doc
	 * @param p
	 * @return Node
	 */
	public static Node format(Document doc, TimePeriod p){
		Element result = doc.createElementNS(PERIOD_NS, PERIOD);
		Element start = doc.createElementNS(PERIOD_NS, START_TIME);
		ReportDateParser format = new ReportDateParser(null);
		start.appendChild(doc.createTextNode(format.format(p.getStart())));
		result.appendChild(start);
		
		if( p instanceof SplitPeriod){
			if(p instanceof CalendarFieldSplitPeriod){
				CalendarFieldSplitPeriod cp = (CalendarFieldSplitPeriod)p;
				int field=cp.getField();
				String field_name="unset";
				switch(field){
				case Calendar.DAY_OF_MONTH: field_name=DAY; break;
				case Calendar.DAY_OF_YEAR: field_name=DAY; break;
				case Calendar.SECOND: field_name=SECOND; break;
				case Calendar.MINUTE: field_name=MINUTE; break;
				case Calendar.HOUR_OF_DAY:
				case Calendar.HOUR: field_name=HOUR; break;
				case Calendar.WEEK_OF_YEAR: field_name=WEEK; break;
				case Calendar.MONTH: field_name=MONTH; break;
				case Calendar.YEAR: field_name=YEAR; break;
				default: throw new IllegalArgumentException("Unsupported field "+field);
				}
				result.appendChild(doc.createElementNS(PERIOD_NS, SPLIT_UNIT)).appendChild(doc.createTextNode(field_name));
				result.appendChild(doc.createElementNS(PERIOD_NS, NUMBER_OF_SPLIT_UNITS)).appendChild(doc.createTextNode(Integer.toString(cp.getCount())));
				result.appendChild(doc.createElementNS(PERIOD_NS, NUMBER_OF_SPLITS)).appendChild(doc.createTextNode(Integer.toString(cp.getNsplit())));

			}else if( p instanceof RegularSplitPeriod){
				Element end = doc.createElementNS(PERIOD_NS, END_TIME);
				end.appendChild(doc.createTextNode(format.format(p.getEnd())));
				result.appendChild(end);
				result.appendChild(doc.createElementNS(PERIOD_NS, NUMBER_OF_SPLITS)).appendChild(doc.createTextNode(Integer.toString(((RegularSplitPeriod)p).getNsplit())));
			}else{
				throw new IllegalArgumentException(p.getClass().getCanonicalName()+" not supported");
			}
		}else{
			Element end = doc.createElementNS(PERIOD_NS, END_TIME);
			end.appendChild(doc.createTextNode(format.format(p.getEnd())));
			result.appendChild(end);
		}
		return result;
	}
	public PeriodExtension(AppContext conn,ReportType type) throws ParserConfigurationException {
		super(conn,type);
	}

	/** get Period start date for display to user
	 * 
	 * @param p
	 * @param format
	 * @return start or formatted string
	 */
	public Object getStart(Period p, String format){
		Date d = p.getStart();
		if( format == null || format.trim().length() == 0 ){
			return d;
		}
		DateFormat df = new SimpleDateFormat(format);
		return df.format(d);
	}
	/** get Period start date for display to user
	 * 
	 * @param p
	 * @param format
	 * @return end or formatted string
	 */
	public Object getEnd(Period p, String format){
		Date d = p.getEnd();
		if( format == null || format.trim().length() == 0 ){
			return d;
		}
		DateFormat df = new SimpleDateFormat(format);
		return df.format(d);
	}
	/** Default period of previous Calendar month
	 * 
	 * @return  Period
	 * @throws Exception 
	 */
	public Period makePeriod() throws Exception{
		return makePeriod(null);
	}
	@Override
	public boolean checkNode(Element e) throws TemplateValidateException {
		if( PERIOD_NS.equals(e.getNamespaceURI()) && e.getLocalName().equals(PERIOD_ELEMENT)){
			try{
				makePeriod(e);
			}catch(UnexpandedContentException e1){
				return true;
			} catch (Exception e2) {
				throw new TemplateValidateException("Cannot parse Period", e2);
			}
		}
		return false;
	}
	
	@Override
	public boolean wantReplace(Element e) {
		return PERIOD_NS.equals(e.getNamespaceURI()) ;
	}
	@Override
	public Node replace(Element e) {
		try {
			String name = e.getLocalName();
			Period p = findPeriodInScope(e);
			String format = e.getAttribute("format");
			switch(name) {
			case "StartDate": return getDocument().createTextNode(getStart(p,format).toString());
			case "EndDate":return getDocument().createTextNode(getEnd(p,format).toString());
			case PERIOD_ELEMENT: return null;
			default:addError("unexpected expansion", name, e); return null;
			}
		}catch(Exception e1) {
			addError("bad_period", "Error formatting period", e, e1);
		}
		return super.replace(e);
	}
}