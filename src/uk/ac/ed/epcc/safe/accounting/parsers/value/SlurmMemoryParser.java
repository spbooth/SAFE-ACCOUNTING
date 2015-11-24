// Copyright - The University of Edinburgh 2011
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.webapp.Description;
@uk.ac.ed.epcc.webapp.Version("$Id: SlurmMemoryParser.java,v 1.4 2015/08/10 11:17:07 spb Exp $")
/** parser for slurm memory values that contain a tailing n for per-node values
 * 
 * @author spb
 *
 */
@Description("Parse a number of bytes in K/M/G")
public class SlurmMemoryParser implements ValueParser<Long> {
    private static final Pattern patt = Pattern.compile("(\\d+)\\s*([kmg]?)b?n?c?",Pattern.CASE_INSENSITIVE);
    private static final Pattern float_patt = Pattern.compile("(\\d+\\.\\d*)\\s*([kmg]?)b?n?c?",Pattern.CASE_INSENSITIVE);
	private static final long K=1024L;
	private static final long M=K*K;
	private static final long G=K*M;
	public static SlurmMemoryParser PARSER = new SlurmMemoryParser();
    public Class<Long> getType() {
		return Long.class;
	}

	public Long parse(String valueString) throws ValueParseException {
		Matcher m = patt.matcher(valueString);
		if( m.matches()){
			long val = Long.parseLong(m.group(1));
			String unit = m.group(2);
			if( unit.equalsIgnoreCase("k")){
				val *= K;
			}else if( unit.equalsIgnoreCase("m")){
				val *= M;
			}else if( unit.equalsIgnoreCase("g")){
				val *= G;
			}
			return Long.valueOf(val);
		}
		m = float_patt.matcher(valueString);
		if( m.matches()){
			double val = Double.parseDouble(m.group(1));
			String unit = m.group(2);
			if( unit.equalsIgnoreCase("k")){
				val *= K;
			}else if( unit.equalsIgnoreCase("m")){
				val *= M;
			}else if( unit.equalsIgnoreCase("g")){
				val *= G;
			}
			return Long.valueOf((long)val);
		}
		
		throw new ValueParseException("cannot parse "+valueString+" as memory");
	}

	public String format(Long value) {
		long val = value.longValue();
		String unit="b";
		if( (val % G) == 0L){
			val /= G;
			unit="gb";
		}else if ((val%M)==0L){
			val /= M;
			unit="mb";
		}else if ((val %K)==0L){
			val /=K;
			unit="kb";
		}
		return Long.toString(val)+unit;
	}

}