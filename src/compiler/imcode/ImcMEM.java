package compiler.imcode;

import compiler.*;

public class ImcMEM extends ImcExpr {
	public final ImcExpr expr;

	public ImcMEM(ImcExpr expr) {
		this.expr = expr;
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "MEM");
		expr.dump(indent + 2);
	}

	@Override
	public ImcESEQ linear() {
		ImcESEQ lin = expr.linear();
		lin.expr = new ImcMEM(lin.expr);
		return lin;
	}
}
