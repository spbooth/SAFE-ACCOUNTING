package uk.ac.ed.epcc.safe.accounting.parsers.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.WebappTestBase;
/** abstract superclass for {@link ValueParser} tests
 * 
 * @author Stephen Booth
 *
 */
public abstract class ValueParserAbstractTest<T> extends WebappTestBase {

	public abstract Class<T> getTarget();
	
	/** get the {@link ValueParser}. By default lookup
	 * from the {@link ValueParserService} using {@link #getTarget()}
	 * 
	 * but override this for non default parsers.
	 * 
	 * @return
	 */
	public ValueParser<T> getValueParser(){
		ValueParserPolicy policy = new ValueParserPolicy(ctx);
		ValueParser<T> result = policy.getValueParser(getTarget());
		assertEquals(getTarget(), result.getType());
		
		return result;
		
	}
	
	public abstract String[]  getParsable();

	@Test
	public void testParse() throws ValueParseException {
		ValueParser<T> parser = getValueParser();
		Class<T> clazz = parser.getType();
		for(String s : getParsable()) {
			T item = parser.parse(s);
			assertNotNull(item);
			assertTrue( clazz.isAssignableFrom(item.getClass()));
			String format = parser.format(item);
			System.out.println(s+"->"+format);
			assertNotNull(format);
			T item2 = parser.parse(format);
			assertEquals(item, item2);
			
		}
	}
	
	public abstract T[] getData();
	@Test
	public void testFormat() throws ValueParseException {
		ValueParser<T> parser = getValueParser();
		Class<T> clazz = parser.getType();
		for(T item : getData()) {
			assertTrue( clazz.isAssignableFrom(item.getClass()));
			String format = parser.format(item);
			assertNotNull(format);
			System.out.println(item.toString()+"->"+format);
			T item2 = parser.parse(format);
			assertEquals(item, item2);
			
		}
	}
	public String[] getErrors() {
		return new String[0];
	}
	
	@Test
	public void testErrors() {
		ValueParser<T> parser = getValueParser();
		for(String s : getErrors()) {
			try {
				parser.parse(s);
				fail("No error parsing "+s);
			}catch(ValueParseException e) {
				// ok expected this
			}
		}
	}
	
	
}
