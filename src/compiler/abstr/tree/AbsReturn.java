package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsReturn extends AbsExpr
{
	public final AbsExpr expr;

	public AbsReturn(Position pos, AbsExpr expr)
	{
		super(pos);
		this.expr = expr;
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }
}
