package uk.ac.ed.epcc.safe.accounting;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;

public class ErrorSetTest {

	public ErrorSetTest() {
		// TODO Auto-generated constructor stub
	}

	
	@Test
	public void testErrorSet() {
		ErrorSet error = new ErrorSet();
		
		error.add("simple", "simple A");
		error.add("simple", "simple B");
		error.add("trace", "trace A", new InvalidArgument("Not A"));
		error.add("trace", "trace B", new InvalidArgument("Not B"));
		addError(error,"A");
		addError(error,"A");
		addError(error,"B");
		assertEquals("3: routine\n"
				+ "	routine\n"
				+ "	routine\n"
				+"2: simple\n"
				+ "	simple A\n"
				+ "	simple B\n"
				+ "2: trace\n"
				+ "	trace A\n"
				+ "	trace B\n"
				+ "", error.toString().replace("\r\n", "\n"));
	}
	
	public void addError(ErrorSet error,String text) {
		error.add("routine", "routine", new InvalidArgument(text));
	}
}
