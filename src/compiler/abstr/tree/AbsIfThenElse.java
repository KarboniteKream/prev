package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsIfThenElse extends AbsExpr {
	public final AbsExpr cond;
	public final AbsExpr thenBody;
	public final AbsExpr elseBody;

	public AbsIfThenElse(Position pos, AbsExpr cond, AbsExpr thenBody, AbsExpr elseBody) {
		super(pos);
		this.cond = cond;
		this.thenBody = thenBody;
		this.elseBody = elseBody;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
