package compiler.imcode;

import java.util.*;

import compiler.*;

public class ImcSEQ extends ImcStmt {
	public final LinkedList<ImcStmt> stmts;

	public ImcSEQ() {
		stmts = new LinkedList<>();
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "SEQ");

		for (ImcStmt stmt : this.stmts) {
			stmt.dump(indent + 2);
		}
	}

	@Override
	public ImcSEQ linear() {
		final ImcSEQ lin = new ImcSEQ();

		for (ImcStmt stmt : this.stmts) {
			final ImcSEQ linStmt = stmt.linear();
			lin.stmts.addAll(linStmt.stmts);
		}

		return lin;
	}
}
