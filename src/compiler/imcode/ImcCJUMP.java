package compiler.imcode;

import compiler.*;
import compiler.frames.*;

public class ImcCJUMP extends ImcStmt {
	public final ImcExpr cond;
	public final FrmLabel trueLabel;
	public final FrmLabel falseLabel;

	public ImcCJUMP(ImcExpr cond, FrmLabel trueLabel, FrmLabel falseLabel) {
		this.cond = cond;
		this.trueLabel = trueLabel;
		this.falseLabel = falseLabel;
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "CJUMP labels=" + trueLabel.name() + "," + falseLabel.name());
		cond.dump(indent + 2);
	}

	@Override
	public ImcSEQ linear() {
		final ImcSEQ lin = new ImcSEQ();
		final ImcESEQ linCond = cond.linear();
		final FrmLabel newFalseLabel = FrmLabel.newLabel();
		lin.stmts.addAll(((ImcSEQ) linCond.stmt).stmts);
		lin.stmts.add(new ImcCJUMP(linCond.expr, trueLabel, newFalseLabel));
		lin.stmts.add(new ImcLABEL(newFalseLabel));
		lin.stmts.add(new ImcJUMP(falseLabel));
		return lin;
	}
}
