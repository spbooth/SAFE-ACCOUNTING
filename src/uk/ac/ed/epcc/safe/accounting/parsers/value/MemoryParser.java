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
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.webapp.Description;


@Description("Parse a number of bytes in K/M/G")
public class MemoryParser implements ValueParser<Long> {
    private static final Pattern patt = Pattern.compile("(\\d+)\\s*([kmg]?)b?",Pattern.CASE_INSENSITIVE);
	private static final long K=1024L;
	private static final long M=K*K;
	private static final long G=K*M;
	public static MemoryParser PARSER = new MemoryParser();
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