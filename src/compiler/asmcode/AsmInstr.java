package compiler.asmcode;

import java.util.*;
import java.util.regex.*;

import compiler.frames.*;

public abstract class AsmInstr {
	public String mnemonic;
	public String assem;

	public final LinkedList<FrmTemp> uses;
	public final LinkedList<FrmTemp> defs;
	public final LinkedList<FrmLabel> labels;

	public LinkedList<FrmTemp> in;
	public LinkedList<FrmTemp> out;

	protected AsmInstr(String mnemonic, String assem, LinkedList<FrmTemp> defs,
			LinkedList<FrmTemp> uses, LinkedList<FrmLabel> labels) {
		this.mnemonic = mnemonic;
		this.assem = assem;
		this.defs = defs == null ? new LinkedList<>() : defs;
		this.uses = uses == null ? new LinkedList<>() : uses;
		this.labels = labels == null ? new LinkedList<>() : labels;

		in = null;
		out = null;
	}

	public String format(HashMap<FrmTemp, String> map) {
		String fmtAssem = String.format("%-5s %s", mnemonic, assem);

		for (int i = 0; i < uses.size(); i++) {
			final FrmTemp temp = uses.get(i);
			String regName = null;
			if (map != null) {
				regName = map.get(temp);
			}
			if (regName == null) {
				regName = temp.name();
			}
			fmtAssem = fmtAssem.replaceAll("`s" + i, Matcher.quoteReplacement(regName));
		}

		for (int i = 0; i < defs.size(); i++) {
			final FrmTemp temp = defs.get(i);
			String regName = null;
			if (map != null) {
				regName = map.get(temp);
			}
			if (regName == null) {
				regName = temp.name();
			}
			fmtAssem = fmtAssem.replaceAll("`d" + i, Matcher.quoteReplacement(regName));
		}

		for (int i = 0; i < labels.size(); i++) {
			final FrmLabel label = labels.get(i);
			fmtAssem = fmtAssem.replaceAll("`l" + i, label.name());
		}

		return fmtAssem;
	}

}
