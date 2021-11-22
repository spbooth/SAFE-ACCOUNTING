package uk.ac.ed.epcc.safe.accounting.formatters.value;

import java.text.NumberFormat;

public class NumberValueFormatter implements ValueFormatter<Number> {

	private final NumberFormat nf;
	public NumberValueFormatter(NumberFormat f) {
		nf=f;
	}

	@Override
	public Class<Number> getType() {
		return Number.class;
	}

	@Override
	public String format(Number object) {
		return nf.format(object);
	}

}
