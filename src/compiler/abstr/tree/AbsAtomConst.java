package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsAtomConst extends AbsExpr {
	public static final int LOG = 0;
	public static final int INT = 1;
	public static final int STR = 2;

	public final int type;
	public final String value;

	public AbsAtomConst(Position pos, int type, String value) {
		super(pos);
		this.type = type;
		this.value = value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
