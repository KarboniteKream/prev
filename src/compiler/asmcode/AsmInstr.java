package compiler.asmcode;

import java.util.*;
import java.util.regex.*;

import compiler.frames.*;

/*
 * Znakovna predstavitev ukaza naj vsebuje obliko, ki je primerna za izpis v zbirnik.
 * Edina razlika je v tem, da znakovna predstavitev namesto registrov in label vsebuje
 * 
 * - `d0, `d1, ... : za zacasne spremenljivke, ki jih ukaz definira;
 * - `s0, `s1, ... : za zacasne spremenljikve, ki jih ukaz uporablja;
 * - `l0, `l1, ... : za labele.
 * 
 * Primera:
 * 1.) Ukaz ADD T0,T0,T1 opisemo kot
 *        "ADD `d0,`s0,`s1" pri defs={T0}, uses={T0,T1}, labels={}
 * 2.) Ukaz BR T3,L5 opisemo kot
 *        "BR `s0,`l0" pri defs={}, uses={T3}, labels={L5}
 *        
 * Metoda 'format' izpise ukaz, a ce argument ni nic (po fazi dodeljevanja registrov),
 * zacasne spremenljivke nadomesti s pravimi registri.
 * 
 */

public abstract class AsmInstr {

	/** Znakovna predstavitev ukaza. */
	public String mnemonic;
	public String assem;

	/** Zacasne spremenljivke, katerih vrednosti ukaz uporabi. */
	public LinkedList<FrmTemp> uses;

	/** Zacasne spremenljivke, katerih vrednosti ukaz doloci. */
	public LinkedList<FrmTemp> defs;

	/** Seznam label, na katerih se lahko nadaljuje izvajanje ukaza. */
	public LinkedList<FrmLabel> labels;

	public LinkedList<FrmTemp> in;
	public LinkedList<FrmTemp> out;

	public boolean spill;

	/**
	 * Opis ukaza strojne kode.
	 * 
	 * @param assem
	 *            Zankovna predstavitev ukaza (glej zgoraj).
	 * @param defs
	 *            Zacasne spremenljivke, ki jih ukaz definira.
	 * @param uses
	 *            Zacasne spremenljivke, ki jih ukaz uporablja.
	 * @param labels
	 *            Labele, na katere ukaz skace.
	 */
	protected AsmInstr(String mnemonic, String assem, LinkedList<FrmTemp> defs,
			LinkedList<FrmTemp> uses, LinkedList<FrmLabel> labels) {
		this.mnemonic = mnemonic;
		this.assem = String.format("%-5s %s", mnemonic, assem);
		this.defs = defs == null ? new LinkedList<FrmTemp>() : defs;
		this.uses = uses == null ? new LinkedList<FrmTemp>() : uses;
		this.labels = labels == null ? new LinkedList<FrmLabel>() : labels;

		in = null;
		out = null;

		spill = false;
	}

	public String format(HashMap<FrmTemp, String> map) {
		String fmtAssem = assem;
		for (int i = 0; i < uses.size(); i++) {
			FrmTemp temp = uses.get(i);
			String regName = null;
			if (map != null)
				regName = map.get(temp);
			if (regName == null)
				regName = temp.name();
			fmtAssem = fmtAssem.replaceAll("`s" + i, Matcher.quoteReplacement(regName));
		}
		for (int i = 0; i < defs.size(); i++) {
			FrmTemp temp = defs.get(i);
			String regName = null;
			if (map != null)
				regName = map.get(temp);
			if (regName == null)
				regName = temp.name();
			fmtAssem = fmtAssem.replaceAll("`d" + i, Matcher.quoteReplacement(regName));
		}
		for (int i = 0; i < labels.size(); i++) {
			FrmLabel label = labels.get(i);
			fmtAssem = fmtAssem.replaceAll("`l" + i, label.name());
		}
		return fmtAssem;
	}

}
