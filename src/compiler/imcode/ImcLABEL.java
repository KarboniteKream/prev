package compiler.imcode;

import compiler.*;
import compiler.frames.*;

public class ImcLABEL extends ImcStmt {
	public final FrmLabel label;

	public ImcLABEL(FrmLabel label) {
		this.label = label;
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "LABEL label=" + label.name());
	}

	@Override
	public ImcSEQ linear() {
		final ImcSEQ lin = new ImcSEQ();
		lin.stmts.add(this);
		return lin;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ImcLABEL) {
			return ((ImcLABEL) obj).label.name().equals(label.name());
		}

		return false;
	}
}
