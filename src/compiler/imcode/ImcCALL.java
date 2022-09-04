package compiler.imcode;

import java.util.*;

import compiler.*;
import compiler.frames.*;

public class ImcCALL extends ImcExpr {
	public final FrmLabel label;
	public final LinkedList<ImcExpr> args;

	public ImcCALL(FrmLabel label) {
		this.label = label;
		this.args = new LinkedList<>();
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "CALL label=" + label.name());

		for (ImcExpr arg : this.args) {
			arg.dump(indent + 2);
		}
	}

	@Override
	public ImcESEQ linear() {
		final ImcSEQ linStmt = new ImcSEQ();
		final ImcCALL linCall = new ImcCALL(label);

		for (ImcExpr imcExpr : this.args) {
			final FrmTemp temp = new FrmTemp();
			final ImcESEQ linArg = imcExpr.linear();
			linStmt.stmts.addAll(((ImcSEQ) linArg.stmt).stmts);
			linStmt.stmts.add(new ImcMOVE(new ImcTEMP(temp), linArg.expr));
			linCall.args.add(new ImcTEMP(temp));
		}

		final FrmTemp temp = new FrmTemp();
		linStmt.stmts.add(new ImcMOVE(new ImcTEMP(temp), linCall));
		return new ImcESEQ(linStmt, new ImcTEMP(temp));
	}
}
