package compiler.tmpan;

import java.util.*;

import compiler.frames.*;

public class TmpNode {
	public static final int POTENTIAL_SPILL = 1;
	public static final int ACTUAL_SPILL = 2;

	public final FrmTemp temp;
	public final LinkedList<TmpNode> edges;

	public int register;
	public int spill;

	public TmpNode(FrmTemp temp) {
		this.temp = temp;
		edges = new LinkedList<>();

		register = 0;
		spill = 0;
	}
}
