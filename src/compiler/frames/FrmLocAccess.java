
package compiler.frames;

import compiler.abstr.tree.*;
import compiler.seman.*;
import compiler.seman.type.*;

public class FrmLocAccess extends FrmAccess {
	public final AbsVarDef var;
	public final FrmFrame frame;
	public final long offset;

	public FrmLocAccess(AbsVarDef var, FrmFrame frame) {
		this.var = var;
		this.frame = frame;

		SemType type = SymbDesc.getType(this.var).actualType();
		this.offset = -frame.sizeLocs - type.size();
		frame.sizeLocs = frame.sizeLocs + type.size();
	}

	@Override
	public String toString() {
		return "LOC(" + var.name + ": offset=" + offset + ")";
	}
}
