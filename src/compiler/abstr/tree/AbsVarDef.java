package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsVarDef extends AbsDef
{
	public final AbsType type;
	public final AbsExpr expr;

	public AbsVarDef(Position pos, String name, AbsType type, AbsExpr expr)
	{
		super(pos, name);
		this.type = type;
		this.expr = expr;
	}

	@Override public void accept(Visitor visitor)
	{
		visitor.visit(this);
	}
}
