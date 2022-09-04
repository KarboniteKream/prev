package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsWhile extends AbsExpr {
	public final AbsExpr cond;
	public final AbsExpr body;

	public AbsWhile(Position pos, AbsExpr cond, AbsExpr body) {
		super(pos);
		this.cond = cond;
		this.body = body;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
