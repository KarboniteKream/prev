package compiler.imcode;

import compiler.*;
import compiler.frames.*;

public class ImcTEMP extends ImcExpr {
	public final FrmTemp temp;

	public ImcTEMP(FrmTemp temp) {
		this.temp = temp;
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "TEMP name=" + temp.name());
	}

	@Override
	public ImcESEQ linear() {
		return new ImcESEQ(new ImcSEQ(), this);
	}
}
