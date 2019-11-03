package uk.ac.ed.epcc.safe.accounting.expr.parse;

import uk.ac.ed.epcc.safe.accounting.expr.ArrayFuncPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.CasePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ComparePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstReferenceExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConvertMillisecondToDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DoubleCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DoubleDeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationSecondsPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.IntPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LabelPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LocatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.LongCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.MilliSecondDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.NamePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.expr.SelectPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.StringPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.TypeConverterPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.MethodPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.FormatException;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.jdbc.expr.ArrayFunc;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** A {@link PropExpressionVisitor} that formats an expression
 * in a way compatible with the parser.
 * 
 * It only needs to be able to handle those expression types that are generated from the parser
 * @author Stephen Booth
 *
 */
public class FormatVisitor implements PropExpressionVisitor<String>{

	@Override
	public String visitPropertyTag(PropertyTag<?> tag) throws Exception {
		return tag.getFullName();
	}

	public String apply(Keywords word, PropExpression ... args) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("@");
		sb.append(word.name());
		sb.append("(");
		boolean seen=false;
		for(PropExpression e : args) {
			if( seen) {
				sb.append(",");
			}
			sb.append(e.accept(this));
			seen=true;
		}
		sb.append(")");
		return sb.toString();
	}
	public <T> String applyIterator(Keywords word, Iterable<PropExpression<T>> set) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("@");
		sb.append(word.name());
		sb.append("(");
		boolean seen=false;
		for(PropExpression e : set) {
			if( seen) {
				sb.append(",");
			}
			sb.append(e.accept(this));
			seen=true;
		}
		sb.append(")");
		return sb.toString();
	}
	public String getMatch(MatchCondition m) {
		if( m == null ) {
			return "==";
		}
		return m.match();
	}
	@Override
	public String visitStringPropExpression(StringPropExpression<?> stringExpression) throws Exception {
		
		return apply(Keywords.STRING,stringExpression.exp);
	}

	@Override
	public String visitIntPropExpression(IntPropExpression<?> intExpression) throws Exception {
		return apply(Keywords.INT, intExpression.exp);
	}

	@Override
	public String visitLongCastPropExpression(LongCastPropExpression<?> intExpression) throws Exception {
		return apply(Keywords.LONG_CAST, intExpression.exp);
	}

	@Override
	public String visitDoubleCastPropExpression(DoubleCastPropExpression<?> doubleExpression) throws Exception {
		return apply(Keywords.DOUBLE_CAST, doubleExpression.exp);
	}

	@Override
	public String visitConstPropExpression(ConstPropExpression<?> constExpression) throws Exception {
		if( constExpression.getTarget().equals(String.class)) {
			return "\""+constExpression.val.toString()+"\"";
		}
		return constExpression.val.toString();
	}

	@Override
	public String visitBinaryPropExpression(BinaryPropExpression binaryPropExpression) throws Exception {
		
		return wrap(binaryPropExpression.a)+binaryPropExpression.op.text()+wrap(binaryPropExpression.b);
	}
	public String wrap(PropExpression<?> e) throws Exception {
		if( e instanceof BinaryPropExpression||e instanceof ComparePropExpression) {
			return "("+e.accept(this)+")";
		}
		return e.accept(this);
	}

	@Override
	public String visitMilliSecondDatePropExpression(MilliSecondDatePropExpression milliSecondDate) throws Exception {
		return apply(Keywords.MILLIS, milliSecondDate.getDateExpression());
	}

	@Override
	public String visitNamePropExpression(NamePropExpression namePropExpression) throws Exception {
		return apply(Keywords.NAME, namePropExpression.getTargetRef());
	}

	@Override
	public <T extends DataObject> String visitDeRefExpression(DeRefExpression<T, ?> deRefExpression) throws Exception {
		return deRefExpression.getTargetObject().accept(this)+"["+deRefExpression.getExpression().accept(this)+"]";
	}

	@Override
	public <T extends DataObject> String visitDoubleDeRefExpression(DoubleDeRefExpression<T, ?> deRefExpression)
			throws Exception {
		return visitDeRefExpression(deRefExpression);
	}

	@Override
	public String visitSelectPropExpression(SelectPropExpression<?> sel) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean seen=false;
		for(PropExpression e : sel) {
			if( seen) {
				sb.append(",");
			}
			sb.append(e.accept(this));
			seen=true;
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public String visitDurationPropExpression(DurationPropExpression sel) throws Exception {
		return apply(Keywords.DURATION,sel.start,sel.end);
	}

	@Override
	public String visitDurationCastPropExpression(DurationCastPropExpression<?> sel) throws Exception {
		return apply(Keywords.DURATION_CAST,sel.exp);
	}

	@Override
	public String visitDurationSecondPropExpression(DurationSecondsPropExpression d) throws Exception {
		return apply(Keywords.DURATION_SECONDS,d.getDuration());
	}

	@Override
	public String visitLocatePropExpression(LocatePropExpression expr) throws Exception {
		return apply(Keywords.LOCATE,expr.substr,expr.str,expr.pos);
	}

	@Override
	public <T extends Comparable> String visitArrayFuncPropExpression(ArrayFuncPropExpression<T> expr)
			throws Exception {
		Keywords word;
		ArrayFunc func = expr.getFunc();
		switch( func) {
		case GREATEST: word = Keywords.GREATEST; break;
		case LEAST: word = Keywords.LEAST; break;
		default: throw new FormatException("Unsupported ArrayFunc "+func.toString());
		}
		return applyIterator(word, expr);
	}

	@Override
	public <T, D> String visitTypeConverterPropExpression(TypeConverterPropExpression<T, D> sel) throws Exception {
		throw new FormatException("Unsupported expression "+sel);
	}

	@Override
	public <T, X> String visitLabelPropExpression(LabelPropExpression<T, X> expr) throws Exception {
		throw new FormatException("Unsupported expression "+expr);
	}

	@Override
	public <T> String visitCasePropExpression(CasePropExpression<T> expr) throws Exception {
		throw new FormatException("Unsupported expression "+expr);
	}

	@Override
	public String visitConvetMillisecondToDateExpression(ConvertMillisecondToDatePropExpression expr) throws Exception {
		return apply(Keywords.DATE,expr.milli_expr);
	}

	@Override
	public <C extends Comparable> String visitCompareExpression(ComparePropExpression<C> expr) throws Exception {
		
		return wrap(expr.e1)+getMatch(expr.m)+wrap(expr.e2);
	}

	@Override
	public <I extends Indexed> String visitConstReferenceExpression(ConstReferenceExpression<I> expr) throws Exception {
		IndexedReference r = expr.val;
		return "@REF("+r.getTag()+","+r.getID()+")";
	}

	@Override
	public String visitMethodPropExpression(MethodPropExpression<?> method) throws Exception {
		throw new FormatException("Unsupported expression "+method);
	}

	

}
