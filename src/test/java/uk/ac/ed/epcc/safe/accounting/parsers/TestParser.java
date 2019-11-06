package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.ReadOnlyParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.convert.EnumProducer;
/** a parser that adds a test property finder with a selection of properties for use in tests
 * 
 * @author Stephen Booth
 *
 */
public class TestParser extends ReadOnlyParser {

	public static final PropertyRegistry test_reg = new PropertyRegistry("test", "Test properties");

	public static final PropertyTag<String> STRING_PROP = new PropertyTag<String>(test_reg, "String", String.class);
	
	public static final PropertyTag<Number> NUMBER_PROP = new PropertyTag<Number>(test_reg, "Number", Number.class);
	public static final PropertyTag<Double> DOUBLE_PROP = new PropertyTag<Double>(test_reg, "Double", Double.class);
	public static final PropertyTag<Duration> DURATION_PROP = new PropertyTag<Duration>(test_reg, "Duration", Duration.class);

	public static final PropertyTag<Date> DATE_PROP = new PropertyTag<Date>(test_reg, "Date", Date.class);

	public static final TypeConverterPropertyTag<Animals> ANIMAL_TAG = new TypeConverterPropertyTag<TestParser.Animals>(test_reg, "Animals", Animals.class, new EnumProducer<Animals>(Animals.class, "Animals"));
	public static enum Animals{
		Cat,
		Dog,
		Walrus,
		Penguin,
		Wombat
	}
	public TestParser(AppContext conn) {
		super(conn);
		
	}
	@Override
	public PropertyFinder initFinder(PropertyFinder prev, String table) {
		MultiFinder finder = new MultiFinder();
		finder.addFinder(super.initFinder(prev, table));
		finder.addFinder(test_reg);
		return finder;
	}

}
