package compiler.frames;

import java.util.*;

import compiler.abstr.tree.*;

public class FrmDesc {
	private static final HashMap<AbsFunDef, FrmFrame> frames = new HashMap<>();
	private static final HashMap<AbsDef, FrmAccess> accesses = new HashMap<>();

	public static void setFrame(AbsFunDef fun, FrmFrame frame) {
		frames.put(fun, frame);
	}

	public static FrmFrame getFrame(AbsTree fun) {
		return frames.get(fun);
	}

	public static void setAccess(AbsDef var, FrmAccess access) {
		accesses.put(var, access);
	}

	public static FrmAccess getAccess(AbsDef var) {
		return accesses.get(var);
	}
}
