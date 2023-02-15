package uk.ac.ed.epcc.safe.accounting.selector;
/** A {@link SelectorVisitor} that generates a deep copy of the argument.
 * This is intended as a base-class for visitors that modify parts of a selector tree
 * 
 * @author Stephen Booth
 *
 */
public class CopySelectorVisitor implements SelectorVisitor<RecordSelector> {

	public CopySelectorVisitor() {
		
	}

	@Override
	public RecordSelector visitAndRecordSelector(AndRecordSelector a) throws Exception {
		AndRecordSelector result = new AndRecordSelector();
		for(RecordSelector s : a) {
			result.add(s.visit(this));
		}
		return result;
	}

	@Override
	public RecordSelector visitOrRecordSelector(OrRecordSelector o) throws Exception {
		OrRecordSelector result = new OrRecordSelector();
		for(RecordSelector s : o) {
			result.add(s.visit(this));
		}
		return result;
	}

	@Override
	public <I> RecordSelector visitClause(SelectClause<I> c) throws Exception {
		
		return c.copy();
	}

	@Override
	public <I> RecordSelector visitNullSelector(NullSelector<I> n) throws Exception {
		return n.copy();
	}

	@Override
	public <I> RecordSelector visitRelationClause(RelationClause<I> c) throws Exception {
		return c.copy();
	}

	@Override
	public RecordSelector visitPeriodOverlapRecordSelector(PeriodOverlapRecordSelector o) throws Exception {
		return o.copy();
	}

	@Override
	public <I> RecordSelector visitOrderClause(OrderClause<I> o) throws Exception {
		return o.copy();
	}

	@Override
	public RecordSelector visitReductionSelector(ReductionSelector r) throws Exception {
		return r.copy();
	}

	@Override
	public RecordSelector visitRelationshipClause(RelationshipClause r) throws Exception {
		return r.copy();
	}

}
