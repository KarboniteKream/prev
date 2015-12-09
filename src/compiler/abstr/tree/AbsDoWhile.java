package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsDoWhile extends AbsExpr
{
	public final AbsExpr body;
	public final AbsExpr cond;

	public AbsDoWhile(Position pos, AbsExpr body, AbsExpr cond)
	{
		super(pos);
		this.body = body;
		this.cond = cond;
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }
}
