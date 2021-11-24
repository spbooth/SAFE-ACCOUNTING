package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BigDecimalParser implements ValueParser<BigDecimal> {

	public BigDecimalParser() {
		
	}

	@Override
	public Class<BigDecimal> getType() {
		return BigDecimal.class;
	}

	@Override
	public BigDecimal parse(String valueString) throws ValueParseException {
		try {
			return new BigDecimal(valueString);
		}catch(NumberFormatException e) {
			throw new ValueParseException("Bad BigInteger value", e);
		}
	}

	@Override
	public String format(BigDecimal value) {
		return value.toString();
	}

	public static final BigDecimalParser PARSER = new BigDecimalParser();
}
