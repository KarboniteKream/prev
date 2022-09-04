package compiler.imcode;

import compiler.*;

public class ImcCONST extends ImcExpr {
	public final Long value;

	public ImcCONST(Long value) {
		this.value = value;
	}

	@Override
	public void dump(int indent) {
		Report.dump(indent, "CONST value=" + value.toString());
	}

	@Override
	public ImcESEQ linear() {
		return new ImcESEQ(new ImcSEQ(), this);
	}
}
