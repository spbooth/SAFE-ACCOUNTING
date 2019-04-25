package uk.ac.ed.epcc.safe.accounting.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.jdbc.expr.ArrayFunc;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

public class EvaluateTests extends WebappTestBase {
	
	public static final PropertyRegistry test = new PropertyRegistry("test", "Test registry");
	public static final PropertyTag<Integer> INT_A = new PropertyTag<>(test,"IntA",Integer.class);
	public static final PropertyTag<Integer> INT_B = new PropertyTag<>(test,"IntB",Integer.class);
	public static final PropertyTag<Double> DOUBLE_A = new PropertyTag<>(test,"DoubleA",Double.class);
	public static final PropertyTag<Double> DOUBLE_B = new PropertyTag<>(test,"DoubleB",Double.class);
	public static final PropertyTag<Date> DATE_A = new PropertyTag<>(test,"DateA",Date.class);
	public static final PropertyTag<Date> DATE_B = new PropertyTag<>(test,"DateB",Date.class);
    public static final PropertyTag<String> STRING_A = new PropertyTag<>(test,"StringA",String.class);
	public static final ReferenceTag<DataObjectPropertyContainer, RemoteTargetFactory> REMOTE = new ReferenceTag<>(test, "Remote", RemoteTargetFactory.class, RemoteTargetFactory.DEFAULT_TABLE);
	public static final PropertyTag<Long> LONG_A = new PropertyTag<>(test,"LongA",Long.class);

	public EvaluateTests() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void addInteger() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(INT_A, 7);
		obj.setProperty(INT_B, 5);
		Number res = obj.evaluateExpression(new BinaryPropExpression(INT_A, Operator.ADD, INT_B));
		assertEquals(12, res);
		assertTrue( res instanceof Integer);
	}
	@Test
	public void subInteger() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(INT_A, 7);
		obj.setProperty(INT_B, 5);
		Number res = obj.evaluateExpression(new BinaryPropExpression(INT_A, Operator.SUB, INT_B));
		assertEquals(2, res);
		assertTrue( res instanceof Integer);
	}
	@Test
	public void mulInteger() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(INT_A, 7);
		obj.setProperty(INT_B, 5);
		Number res = obj.evaluateExpression(new BinaryPropExpression(INT_A, Operator.MUL, INT_B));
		assertEquals(35, res);
		assertTrue( res instanceof Integer);
	}
	@Test
	public void divInteger() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(INT_A, 10);
		obj.setProperty(INT_B, 5);
		Number res = obj.evaluateExpression(new BinaryPropExpression(INT_A, Operator.DIV, INT_B));
		assertEquals(2, res);
		assertTrue( res instanceof Integer);
	}
	@Test
	public void divInteger2() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(INT_A, 7);
		obj.setProperty(INT_B, 5);
		Number res = obj.evaluateExpression(new BinaryPropExpression(INT_A, Operator.DIV, INT_B));
		assertEquals(1.4, res);
		assertTrue( res instanceof Double);
	}
	@Test
	public void addDouble() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(DOUBLE_A, 7.0);
		obj.setProperty(DOUBLE_B, 5.0);
		Number res = obj.evaluateExpression(new BinaryPropExpression(DOUBLE_A, Operator.ADD, DOUBLE_B));
		assertEquals(12.0, res);
		assertTrue( res instanceof Double);
	}
	@Test
	public void addConstDouble() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(DOUBLE_A, 7.0);
		//obj.setProperty(DOUBLE_B, 5.0);
		Number res = obj.evaluateExpression(new BinaryPropExpression(DOUBLE_A, Operator.ADD, new ConstPropExpression<>(Double.class, 5.0)));
		assertEquals(12.0, res);
		assertTrue( res instanceof Double);
	}
	
	@Test
	public void testDuration() throws InvalidPropertyException {
		Calendar c = Calendar.getInstance();
		
		c.clear();
		c.set(1965,Calendar.DECEMBER, 12);
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(DATE_A, c.getTime());
		
		c.set(1965,Calendar.DECEMBER,25);
		obj.setProperty(DATE_B, c.getTime());
		
		Duration d = obj.evaluateExpression(new DurationPropExpression(DATE_A, DATE_B));
		assertEquals(13*24*60*60, d.getSeconds());
	}
	@Test
	public void testDurationSeconds() throws InvalidPropertyException {
		Calendar c = Calendar.getInstance();
		
		c.clear();
		c.set(1965,Calendar.DECEMBER, 12);
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(DATE_A, c.getTime());
		
		c.set(1965,Calendar.DECEMBER,25);
		obj.setProperty(DATE_B, c.getTime());
		
		Number n = obj.evaluateExpression(new DurationSecondsPropExpression(new DurationPropExpression(DATE_A, DATE_B)));
		assertEquals(13*24*60*60, n.intValue());
	}
	@Test
	public void testMiliisecondDate() throws InvalidPropertyException, PropertyCastException {
		Calendar c = Calendar.getInstance();
		
		c.clear();
		c.set(1965,Calendar.DECEMBER, 12);
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(DATE_A, c.getTime());
		
		long millis = c.getTime().getTime();
		Long result = obj.evaluateExpression(new MilliSecondDatePropExpression(DATE_A));
		assertEquals(millis, result.longValue());
		
	}
	
	@Test
	public void testRemoteExpression() throws InvalidPropertyException, DataFault, PropertyCastException {
		RemoteTargetFactory fac = new RemoteTargetFactory<>(ctx);
		ExpressionTargetFactory etf = ExpressionCast.getExpressionTargetFactory(fac);
		DataObjectPropertyContainer rec = (DataObjectPropertyContainer) fac.makeBDO();
		ExpressionTargetContainer proxy = etf.getExpressionTarget(rec);
		proxy.setProperty(RemoteTargetFactory.INT_A, 7);
		proxy.setProperty(RemoteTargetFactory.INT_B, 5);
		proxy.commit();
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		REMOTE.set(obj, rec);
		obj.setProperty(INT_A, 2);
		Number res = obj.evaluateExpression(new BinaryPropExpression(INT_A, Operator.MUL, 
				new DeRefExpression<>(REMOTE, new BinaryPropExpression(RemoteTargetFactory.INT_A, Operator.ADD, RemoteTargetFactory.INT_B))
				));
		assertEquals(24, res);
		assertTrue( res instanceof Integer);
		
		String name = obj.evaluateExpression(new NamePropExpression(REMOTE));
		assertEquals(rec.getIdentifier(),name);
		
	}
	
	@Test
	public void stringExpression() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(INT_A, 7);
		obj.setProperty(INT_B, 5);
		Object res = obj.evaluateExpression(new StringPropExpression<>(new BinaryPropExpression(INT_A, Operator.ADD, INT_B)));
		assertEquals("12", res);
		assertTrue( res instanceof String);
	}
	
	@Test
	public void intExpression() throws InvalidPropertyException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(STRING_A, "12");
		Object res = obj.evaluateExpression(new IntPropExpression<>(STRING_A));
		assertEquals(12, res);
		assertTrue( res instanceof Integer);
	}
	
	@Test
	public void intExpression2() throws InvalidPropertyException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(DOUBLE_A, 12.0);
		Object res = obj.evaluateExpression(new IntPropExpression<>(DOUBLE_A));
		assertEquals(12, res);
		assertTrue( res instanceof Integer);
	}
	@Test
	public void longExpression() throws InvalidPropertyException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(STRING_A, "12");
		Object res = obj.evaluateExpression(new LongCastPropExpression<>(STRING_A));
		assertEquals(12L, res);
		assertTrue( res instanceof Long);
	}
	
	@Test
	public void longExpression2() throws InvalidPropertyException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(DOUBLE_A, 12.0);
		Object res = obj.evaluateExpression(new LongCastPropExpression<>(DOUBLE_A));
		assertEquals(12L, res);
		assertTrue( res instanceof Long);
	}
	@Test
	public void doubleExpression() throws InvalidPropertyException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(STRING_A, "12");
		Object res = obj.evaluateExpression(new DoubleCastPropExpression<>(STRING_A));
		assertEquals(12.0, res);
		assertTrue( res instanceof Double);
	}
	
	@Test
	public void doubleExpression2() throws InvalidPropertyException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(INT_A, 12);
		Object res = obj.evaluateExpression(new DoubleCastPropExpression<>(INT_A));
		assertEquals(12.0, res);
		assertTrue( res instanceof Double);
	}
	
	@Test
	public void durationCastExpression() throws InvalidPropertyException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(DOUBLE_A, 12.0);
		Object res = obj.evaluateExpression(new DurationCastPropExpression<>(DOUBLE_A,1000L));
		assertTrue( res instanceof Duration);
		assertEquals(12, ((Duration)res).getSeconds());
	}
	@Test
	public void testMillisecondToDate() throws InvalidPropertyException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		Calendar c = Calendar.getInstance();
		
		c.clear();
		c.set(1965,Calendar.DECEMBER, 12);
		obj.setProperty(LONG_A, c.getTimeInMillis());
		
		Object res = obj.evaluateExpression(new ConvertMillisecondToDatePropExpression(LONG_A));
		assertTrue(res instanceof Date);
		assertEquals(c.getTime(),res);
		
	}
	@Test
	public void arrayFunc() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(INT_A, 7);
		obj.setProperty(INT_B, 5);
		Number res = obj.evaluateExpression(new ArrayFuncPropExpression<Integer>(Integer.class, ArrayFunc.GREATEST,new PropExpression[] { INT_A,INT_B}));
		assertEquals(7, res);
		assertTrue( res instanceof Integer);
		res = obj.evaluateExpression(new ArrayFuncPropExpression<Integer>(Integer.class, ArrayFunc.LEAST,new PropExpression[] { INT_A,INT_B}));
		assertEquals(5, res);
		assertTrue( res instanceof Integer);
	}
	@Test
	public void arrayFuncDate() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		Calendar c = Calendar.getInstance();
		
		c.clear();
		c.set(1965,Calendar.DECEMBER, 12);
		
		Date least = c.getTime();
		obj.setProperty(DATE_A, least);
		
		c.set(1965,Calendar.DECEMBER,25);
		Date later = c.getTime();
		obj.setProperty(DATE_B, later);
		Date res = obj.evaluateExpression(new ArrayFuncPropExpression<Date>(Date.class, ArrayFunc.LEAST,new PropExpression[] { DATE_A,DATE_B}));
		assertEquals(least, res);
		
		res = obj.evaluateExpression(new ArrayFuncPropExpression<Date>(Date.class, ArrayFunc.GREATEST,new PropExpression[] { DATE_A,DATE_B}));
		assertEquals(later, res);
	}
	
	@Test
	public void compare() throws InvalidPropertyException, PropertyCastException {
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		obj.setProperty(INT_A, 7);
		obj.setProperty(INT_B, 5);
		Boolean res = obj.evaluateExpression(new ComparePropExpression<>(INT_A, MatchCondition.GT, INT_B));
		assertTrue(res);
		res = obj.evaluateExpression(new ComparePropExpression<>(INT_A, null, INT_B));
		assertFalse(res);
		
		res = obj.evaluateExpression(new ComparePropExpression<>(INT_A, MatchCondition.LT, INT_B));
		assertFalse(res);
	
		obj.setProperty(INT_B,7 );
		res = obj.evaluateExpression(new ComparePropExpression<>(INT_A, null, INT_B));
		assertTrue(res);
	}
	@Test
	public void compareDate() throws InvalidPropertyException, PropertyCastException {
		Calendar c = Calendar.getInstance();
		
		c.clear();
		c.set(1965,Calendar.DECEMBER, 12);
		DerivedPropertyMap obj = new DerivedPropertyMap(ctx);
		Date least = c.getTime();
		obj.setProperty(DATE_A, least);
		
		c.set(1965,Calendar.DECEMBER,25);
		Date later = c.getTime();
		obj.setProperty(DATE_B, later);
		Boolean res = obj.evaluateExpression(new ComparePropExpression<>(DATE_B, MatchCondition.GT, DATE_A));
		assertTrue(res);
		res = obj.evaluateExpression(new ComparePropExpression<>(DATE_A, null, DATE_B));
		assertFalse(res);
		
		res = obj.evaluateExpression(new ComparePropExpression<>(DATE_B, MatchCondition.LT, DATE_A));
		assertFalse(res);
	
		obj.setProperty(DATE_B,least );
		res = obj.evaluateExpression(new ComparePropExpression<>(DATE_A, null, DATE_B));
		assertTrue(res);
	}
}
