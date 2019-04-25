//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.BaseParser;
import uk.ac.ed.epcc.safe.accounting.update.OptionalTable;
import uk.ac.ed.epcc.webapp.AppContext;
/** Parser for GridFtp data transfer logs
 * 
 * @author spb
 *
 */
public class GridFtpTransferParser extends BaseParser {
	

	public GridFtpTransferParser(AppContext conn) {
		super(conn);
	}

	private static final String patt="^DATE=(\\d{14}.\\d{6}) HOST=(.*) PROG=(.*) NL.EVNT=.* START=(\\d{14}.\\d{6}) USER=(.*) FILE=(.*) BUFFER=(\\d*) BLOCK=(\\d*) NBYTES=(\\d*) VOLUME=(.*) STREAMS=(\\d*) STRIPES=(\\d*) DEST=\\[(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\] TYPE=(.*) CODE=(\\d*)$";
	private static final Pattern p = Pattern.compile(patt);
	private static final DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss.SSS");
	public static final PropertyRegistry gftp_reg = new PropertyRegistry("gridftp", "GridFtp properties");
	@AutoTable
	public static PropertyTag<Date> start = new PropertyTag<>(gftp_reg, "Start", Date.class);
	@AutoTable(unique=true)
	public static PropertyTag<Date> end = new PropertyTag<>(gftp_reg, "End", Date.class);
	@AutoTable(unique=true)
	public static PropertyTag<String> user = new PropertyTag<>(gftp_reg, "User", String.class);

	public static PropertyTag<String> host = new PropertyTag<>(gftp_reg, "Host", String.class);
	public static PropertyTag<String> prog = new PropertyTag<>(gftp_reg, "Prog", String.class);
	@AutoTable(length=512)
	public static PropertyTag<String> file = new PropertyTag<>(gftp_reg, "File", String.class);
	@OptionalTable
	public static PropertyTag<Number> buffer = new PropertyTag<>(gftp_reg,"Buffer",Number.class);
	@OptionalTable
	public static PropertyTag<Number> block = new PropertyTag<>(gftp_reg,"Block",Number.class);
	@AutoTable
	public static PropertyTag<Number> bytes = new PropertyTag<>(gftp_reg,"Nbytes",Number.class);
	@AutoTable(target=Integer.class)
	public static PropertyTag<Number> streams = new PropertyTag<>(gftp_reg,"Streams",Number.class);
	@AutoTable(target=Integer.class)
	public static PropertyTag<Number> stripes = new PropertyTag<>(gftp_reg,"Stripes",Number.class);
	public static PropertyTag<Number> code = new PropertyTag<>(gftp_reg,"Code",Number.class);
	@AutoTable(unique=true)
	public static PropertyTag<String> remote = new PropertyTag<>(gftp_reg, "RemoteHost", String.class);
	public static PropertyTag<String> volume = new PropertyTag<>(gftp_reg, "Volume", String.class);
	@AutoTable
	public static PropertyTag<String> type = new PropertyTag<>(gftp_reg, "Type", String.class);

	
	static{
		gftp_reg.lock();
	}
	@Override
	public boolean parse(DerivedPropertyMap map, String record)
			throws AccountingParseException {
		if( record == null || record.trim().length() == 0){
			return false;
		}
		Matcher m = p.matcher(record);
		if( ! m.matches()){
			throw new AccountingParseException("Unexpected pattern");
		}
		try {
			map.setProperty(end, df.parse(m.group(1)));
			map.setProperty(host, m.group(2));
			map.setProperty(prog, m.group(3));
			map.setProperty(start, df.parse(m.group(4)));
			map.setProperty(user, m.group(5));
			map.setProperty(file,m.group(6));
			map.setProperty(buffer,Long.parseLong(m.group(7)));
			map.setProperty(block, Integer.parseInt(m.group(8)));
			map.setProperty(bytes, Long.parseLong(m.group(9)));
			map.setProperty(volume, m.group(10));
			map.setProperty(streams, Integer.parseInt(m.group(11)));
			map.setProperty(stripes, Integer.parseInt(m.group(12)));
			map.setProperty(remote, m.group(13));
			map.setProperty(type, m.group(14));
			map.setProperty(code, Integer.parseInt(m.group(15)));
			
		} catch (java.text.ParseException e) {
			throw new AccountingParseException("Bad field format", e);
		}
		return true;
	}
	@Override
	public PropertyFinder initFinder( PropertyFinder prev,
			String table) {
		MultiFinder mf = (MultiFinder) super.initFinder( prev, table);
		mf.addFinder(gftp_reg);
		return mf;
	}

	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap res) {
		
		PropExpressionMap result = super.getDerivedProperties(res);
		try{
			result.put(StandardProperties.STARTED_PROP,start);
			result.put(StandardProperties.ENDED_PROP,end);
			result.put(StandardProperties.USERNAME_PROP,user);
		}catch(PropertyCastException e){
			
		}
		return result;
	}
}