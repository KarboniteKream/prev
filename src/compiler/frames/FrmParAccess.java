package compiler.frames;

import compiler.abstr.tree.*;
import compiler.seman.*;
import compiler.seman.type.*;

public class FrmParAccess extends FrmAccess {
	public final AbsPar par;
	public final FrmFrame frame;
	public final long offset;

	public FrmParAccess(AbsPar par, FrmFrame frame) {
		this.par = par;
		this.frame = frame;

		final SemType type = SymbDesc.getType(this.par).actualType();
		this.offset = frame.sizePars;
		frame.sizePars = frame.sizePars + type.size();
		frame.numPars++;
	}

	@Override
	public String toString() {
		return "PAR(" + par.name + ": offset=" + offset + ")";
	}
}
