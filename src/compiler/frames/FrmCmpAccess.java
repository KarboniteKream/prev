package compiler.frames;

import compiler.abstr.tree.*;

public class FrmCmpAccess extends FrmAccess
{
	public AbsComp cmp;
	public int offset;

	public FrmCmpAccess(AbsComp cmp, int offset) {
		this.cmp = cmp;
		this.offset = offset;
	}

	@Override
	public String toString() {
		return "CMP(" + cmp.name + ": offset=" + offset + ")";
	}
}
