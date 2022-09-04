package compiler.imcode;

import compiler.*;

public class ImcESEQ extends ImcExpr {
	public ImcStmt stmt;
	public ImcExpr expr;

	public ImcESEQ(ImcStmt stmt, ImcExpr expr) {
		this.stmt = stmt;
		this.expr = expr;
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "ESEQ");
		stmt.dump(indent + 2);
		expr.dump(indent + 2);
	}

	@Override
	public ImcESEQ linear() {
		final ImcSEQ linStmt = stmt.linear();
		final ImcESEQ linExpr = expr.linear();
		linStmt.stmts.addAll(((ImcSEQ) linExpr.stmt).stmts);
		linExpr.stmt = linStmt;
		return linExpr;
	}
}
