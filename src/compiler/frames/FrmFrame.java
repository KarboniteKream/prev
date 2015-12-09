package compiler.frames;

import java.util.*;

import compiler.abstr.tree.*;

public class FrmFrame
{
	/** Opis funckije.  */
	public AbsFunDef fun;

	/** Vstopna labela.  */
	public FrmLabel label;
	public FrmLabel endLabel;

	public int numPars;
	public int sizePars;

	LinkedList<FrmLocAccess> locVars;
	public int sizeLocs;

	/** Velikost bloka za oldFP in retAddr.  */
	// ali oldfp sploh potrebujemo?
	public int sizeFPRA;

	/** Velikost bloka zacasnih spremenljivk.  */
	public int sizeTmps;
	public int sizeArgs;

	public FrmTemp FP;
	/** Spremenljivka z rezultatom funkcije.  */
	public FrmTemp RV;
	public FrmTemp SP;

	public FrmFrame(AbsFunDef fun)
	{
		this.fun = fun;
		this.label = FrmLabel.newLabel(fun.name);
		this.endLabel = FrmLabel.newLabel("_" + fun.name);
		this.numPars = 0;
		this.sizePars = 8;
		this.locVars = new LinkedList<FrmLocAccess> ();
		this.sizeLocs = 0;
		this.sizeFPRA = 6;
		this.sizeTmps = 0;
		this.sizeArgs = 0;
		FP = new FrmTemp();
		RV = new FrmTemp();
		SP = new FrmTemp();
	}

	public int size()
	{
		return sizeLocs + sizeFPRA + sizeTmps + sizeArgs;
	}

	@Override
	public String toString()
	{
		return ("FRAME(" + fun.name + ": " + "label=" + label.name() + "," +
				"sizeLocs=" + sizeLocs + "," + "sizeArgs=" + sizeArgs + "," +
				"size=" + size() + "," + "FP=" + FP.name() + "," +
				"RV=" + RV.name() + "," + "SP=" + SP.name() + ")");
	}
}
