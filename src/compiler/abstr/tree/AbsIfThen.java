package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsIfThen extends AbsExpr {
	public final AbsExpr cond;
	public final AbsExpr thenBody;

	public AbsIfThen(Position pos, AbsExpr cond, AbsExpr thenBody) {
		super(pos);
		this.cond = cond;
		this.thenBody = thenBody;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
