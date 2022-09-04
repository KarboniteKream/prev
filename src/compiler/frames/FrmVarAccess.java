package compiler.frames;

import compiler.abstr.tree.*;

public class FrmVarAccess extends FrmAccess {
	public final AbsVarDef var;
	public final FrmLabel label;

	public FrmVarAccess(AbsVarDef var) {
		this.var = var;
		label = FrmLabel.newLabel(var.name);
	}

	@Override
	public String toString() {
		return "VAR(" + var.name + ": label=" + label.name() + ")";
	}
}
