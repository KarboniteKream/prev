package compiler.asmcode;

import compiler.frames.*;

/**
 * Opis ukazov za prenos vrednosti ene zacasne spremenljivke v drugo.
 * 
 * @author sliva
 */
public class AsmMOVE extends AsmInstr {

	public AsmMOVE(String assem, FrmTemp def, FrmTemp use) {
		super(assem, null, null, null);
		defs.add(def);
		uses.add(use);
	}

}
