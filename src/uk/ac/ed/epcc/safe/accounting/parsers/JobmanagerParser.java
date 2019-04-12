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
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DateParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.webapp.AppContext;
/** parser for EGEE jobmanger logs
 * 
 * @author spb
 *
 */


public class JobmanagerParser extends AbstractPropertyContainerParser {
	
	public JobmanagerParser(AppContext conn) {
		super(conn);
	}
	private static final Pattern arg_pattern = Pattern.compile("\"(\\w+)=([^\"]*)\"");
    public static final PropertyRegistry jobmanager = new PropertyRegistry("JobmanagerParser","Properties generated by the JobManager parser");
    public static final PropertyTag<Integer> localUser=new PropertyTag<>(jobmanager,"localUser",Integer.class);
    @AutoTable(length=128) public static final PropertyTag<String> userDN=new PropertyTag<>(jobmanager,"userDN",String.class);
    @AutoTable(length=128) public static final PropertyTag<String> userFQAN=new PropertyTag<>(jobmanager,"userFQAN",String.class);
    @AutoTable(unique=true,length=64) public static final PropertyTag<String> jobID=new PropertyTag<>(jobmanager,"jobID",String.class);
    @AutoTable(length=64) public static final PropertyTag<String> ceID=new PropertyTag<>(jobmanager,"ceID",String.class);
    @AutoTable(length=128) public static final PropertyTag<String> lrmsID=new PropertyTag<>(jobmanager,"lrmsID",String.class);
    @AutoTable(unique=true) public static final PropertyTag<Date> timestamp=new PropertyTag<>(jobmanager,"timestamp",Date.class);
    private final static DateFormat df = new SimpleDateFormat("y-M-d H:m:s");
 
	private ValueParserPolicy vis;
	
	@Override
	public String endParse() {
		vis=null;
		return "";
	}
	@Override
	public void startParse(PropertyContainer staticProps) throws Exception {
		vis = new ValueParserPolicy(getContext()){

			@Override
			public ValueParser makeValueParser(Class clazz) {
				if( clazz == Date.class ){
					return new DateParser(df);
				}
				return super.makeValueParser(clazz);
			}
			
		};
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean parse(DerivedPropertyMap map, String record)
			throws AccountingParseException {
		Matcher m = arg_pattern.matcher(record);
		while( m.find() ){
			String name= m.group(1);
			String value=m.group(2);
			PropertyTag p = jobmanager.find(name);
			if( p != null ){
				
				try {
					ValueParser vp = (ValueParser) p.accept(vis);
					map.setProperty(p, vp.parse(value));
				} catch (Exception e) {
					throw new AccountingParseException("Failed to parse "+name,e);
				}
			}
		}
		return true;
	}
	@Override
	public PropertyFinder initFinder(PropertyFinder prev,
			String table) {
	
		return jobmanager;
	}


}