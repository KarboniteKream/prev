package compiler.asmcode;

import compiler.frames.*;

public class AsmLABEL extends AsmInstr {
	public AsmLABEL(String assem, FrmLabel label) {
		super(assem, "", null, null, null);
		labels.add(label);
	}
}
