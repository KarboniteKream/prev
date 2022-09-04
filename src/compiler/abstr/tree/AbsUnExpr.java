package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsUnExpr extends AbsExpr {
	public static final int ADD = 0;
	public static final int SUB = 1;
	public static final int MEM = 2; // (prefix ^)
	public static final int VAL = 3; // (postfix ^)
	public static final int NOT = 4;

	public final int oper;
	public final AbsExpr expr;

	public AbsUnExpr(Position pos, int oper, AbsExpr expr) {
		super(pos);
		this.oper = oper;
		this.expr = expr;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
