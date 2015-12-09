package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsNop extends AbsExpr
{
	public AbsNop(Position pos)
	{
		super(pos);
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }
}
