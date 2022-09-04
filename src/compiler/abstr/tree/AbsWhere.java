package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsWhere extends AbsExpr {
	public final AbsExpr expr;
	public final AbsDefs defs;

	public AbsWhere(Position pos, AbsExpr expr, AbsDefs defs) {
		super(pos);
		this.expr = expr;
		this.defs = defs;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
