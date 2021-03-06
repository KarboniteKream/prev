package compiler.frames;

import java.util.*;

import compiler.abstr.tree.*;

/**
 * Klicni zapis funkcije.
 *
 * @author sliva
 */
public class FrmFrame {

	/** Opis funckije.  */
	public AbsFunDef fun;

	/** Staticni nivo funkcije.  */
	public int level;

	/** Vstopna labela.  */
	public FrmLabel label;

	/** Stevilo parametrov.  */
	public int numPars;

	/** Velikost bloka parametrov.  */
	public long sizePars;

	/** Lokalne spremenljivke podprograma.  */
	LinkedList<FrmLocAccess> locVars;

	/** Velikost bloka lokalnih spremenljivk.  */
	public long sizeLocs;

	/** Velikost bloka za oldFP in retAddr.  */
	public int sizeFPRA;

	/** Velikost bloka zacasnih spremenljivk.  */
	public long sizeTmps;

	/** Velikost bloka registrov.  */
	public long sizeRegs;

	/** Velikost izhodnih argumentov.  */
	public long sizeArgs;

	/** Kazalec FP.  */
	public FrmTemp FP;

	/** Spremenljivka z rezultatom funkcije.  */
	public FrmTemp RV;

	/** Kazalec SP.  */
	public FrmTemp SP;

	/**
	 * Ustvari nov klicni zapis funkcije.
	 *
	 * @param fun Funkcija.
	 * @param level Staticni nivo funkcije.
	 */
	public FrmFrame(AbsFunDef fun, int level) {
		this.fun = fun;
		this.level = level;
		this.label = (level == 1 ? FrmLabel.newLabel(fun.name) : FrmLabel.newLabel());
		this.numPars = 0;
		this.sizePars = 8;
		this.locVars = new LinkedList<FrmLocAccess> ();
		this.sizeLocs = 0;
		this.sizeFPRA = 16;
		this.sizeTmps = 0;
		this.sizeRegs = 0;
		this.sizeArgs = 0;
		FP = new FrmTemp();
		RV = new FrmTemp();
		SP = new FrmTemp();
	}

	/** Velikost klicnega zapisa.  */
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
