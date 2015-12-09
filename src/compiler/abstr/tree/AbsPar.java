package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsPar extends AbsDef
{
	public final AbsType type;

	public AbsPar(Position pos, String name, AbsType type)
	{
		super(pos, name);
		this.type = type;
	}

	public void accept(Visitor visitor)
	{
		visitor.visit(this);
	}
}
