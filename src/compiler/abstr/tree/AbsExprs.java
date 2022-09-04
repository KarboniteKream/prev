package compiler.abstr.tree;

import java.util.*;

import compiler.*;
import compiler.abstr.*;

public class AbsExprs extends AbsExpr {
	private final AbsExpr[] exprs;

	public AbsExprs(Position pos, Vector<AbsExpr> exprs) {
		super(pos);

		this.exprs = new AbsExpr[exprs.size()];
		for (int expr = 0; expr < exprs.size(); expr++) {
			this.exprs[expr] = exprs.elementAt(expr);
		}
	}

	public AbsExpr expr(int index) {
		return exprs[index];
	}

	public int numExprs() {
		return exprs.length;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
