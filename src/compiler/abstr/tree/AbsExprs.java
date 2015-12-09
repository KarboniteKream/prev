package compiler.abstr.tree;

import java.util.*;

import compiler.*;
import compiler.abstr.*;

public class AbsExprs extends AbsExpr
{
	private final Vector<AbsTree> exprs;

	public AbsExprs(Position pos, Vector<AbsTree> exprs)
	{
		super(pos);
		this.exprs = exprs;
	}

	public AbsTree expr(int index)
	{
		return exprs.elementAt(index);
	}

	public int numExprs()
	{
		return exprs.size();
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }

}
