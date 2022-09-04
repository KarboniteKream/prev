package compiler.frames;

import java.util.*;

import compiler.abstr.tree.*;

public class FrmFrame {
	public final AbsFunDef fun;
	public final int level;
	public final FrmLabel label;
	public int numPars;
	public long sizePars;
	final LinkedList<FrmLocAccess> locVars;
	public long sizeLocs;
	public final int sizeFPRA;
	public long sizeTmps;
	public final long sizeRegs;
	public long sizeArgs;
	public final FrmTemp FP;
	public final FrmTemp RV;
	public final FrmTemp SP;

	public FrmFrame(AbsFunDef fun, int level) {
		this.fun = fun;
		this.level = level;
		this.label = (level == 1 ? FrmLabel.newLabel(fun.name) : FrmLabel.newLabel());
		this.numPars = 0;
		this.sizePars = 8;
		this.locVars = new LinkedList<>();
		this.sizeLocs = 0;
		this.sizeFPRA = 16;
		this.sizeTmps = 0;
		this.sizeRegs = 0;
		this.sizeArgs = 0;
		FP = new FrmTemp();
		RV = new FrmTemp();
		SP = new FrmTemp();
	}

	public long size() {
		return sizeLocs + sizeFPRA + sizeTmps + sizeRegs + sizeArgs;
	}

	@Override
	public String toString() {
		return ("FRAME(" + fun.name + ": " +
					"level=" + level + "," +
					"label=" + label.name() + "," +
					"sizeLocs=" + sizeLocs + "," +
					"sizeArgs=" + sizeArgs + "," +
					"size=" + size() + "," +
					"FP=" + FP.name() + "," +
					"RV=" + RV.name() + "," +
					"SP=" + SP.name() + ")");
	}
}
