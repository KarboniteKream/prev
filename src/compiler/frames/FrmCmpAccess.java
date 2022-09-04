package compiler.frames;

import compiler.abstr.tree.*;

public class FrmCmpAccess extends FrmAccess {
	public final AbsComp cmp;
	public final long offset;

	public FrmCmpAccess(AbsComp cmp, long offset) {
		this.cmp = cmp;
		this.offset = offset;
	}

	@Override
	public String toString() {
		return "CMP(" + cmp.name + ": offset=" + offset + ")";
	}
}
