package compiler.imcode;

import compiler.*;

public class ImcMOVE extends ImcStmt {
	public final ImcExpr dst;
	public final ImcExpr src;

	public ImcMOVE(ImcExpr dst, ImcExpr src) {
		this.dst = dst;
		this.src = src;
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "MOVE");
		dst.dump(indent + 2);
		src.dump(indent + 2);
	}

	@Override
	public ImcSEQ linear() {
		final ImcSEQ lin = new ImcSEQ();
		final ImcESEQ dst = this.dst.linear();
		final ImcESEQ src = this.src.linear();
		lin.stmts.addAll(((ImcSEQ) dst.stmt).stmts);
		lin.stmts.addAll(((ImcSEQ) src.stmt).stmts);
		lin.stmts.add(new ImcMOVE(dst.expr, src.expr));
		return lin;
	}
}
