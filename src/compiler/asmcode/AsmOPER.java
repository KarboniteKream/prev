package compiler.asmcode;

import java.util.*;

import compiler.frames.*;

/**
 * Opis ukazov, ki se jih ne opise z AsmLABEL in AsmMOVE.
 * 
 * @author sliva
 */
public class AsmOPER extends AsmInstr {

	public AsmOPER(String mnemonic, String assem, LinkedList<FrmTemp> defs, LinkedList<FrmTemp> uses, LinkedList<FrmLabel> labels) {
		super(mnemonic, assem, defs, uses, labels);
	}

	public AsmOPER(String mnemonic, String assem, LinkedList<FrmTemp> defs, LinkedList<FrmTemp> uses) {
		super(mnemonic, assem, defs, uses, null);
	}

}
