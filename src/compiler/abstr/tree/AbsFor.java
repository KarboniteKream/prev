package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsFor extends AbsExpr
{
	public final AbsTree init;
	public final AbsExpr cond;
	public final AbsExpr step;
	public final AbsExpr body;

	public AbsFor(Position pos, AbsTree init, AbsExpr cond, AbsExpr step, AbsExpr body)
	{
		super(pos);
		this.init = init;
		this.cond = cond;
		this.step = step;
		this.body = body;
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }
}
