package compiler.asmcode;

import java.util.*;

import compiler.frames.*;

public class AsmOPER extends AsmInstr {
	public AsmOPER(String mnemonic, String assem, LinkedList<FrmTemp> defs, LinkedList<FrmTemp> uses, LinkedList<FrmLabel> labels) {
		super(mnemonic, assem, defs, uses, labels);
	}

	public AsmOPER(String mnemonic, String assem, LinkedList<FrmTemp> defs, LinkedList<FrmTemp> uses) {
		super(mnemonic, assem, defs, uses, null);
	}

	public AsmOPER(String mnemonic, String assem) {
		super(mnemonic, assem, null, null, null);
	}
}
