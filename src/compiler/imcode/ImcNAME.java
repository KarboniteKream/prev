package compiler.imcode;

import compiler.*;
import compiler.frames.*;

public class ImcNAME extends ImcExpr {
	public final FrmLabel label;

	public ImcNAME(FrmLabel label) {
		this.label = label;
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "NAME label=" + label.name());
	}

	@Override
	public ImcESEQ linear() {
		return new ImcESEQ(new ImcSEQ(), this);
	}
}
