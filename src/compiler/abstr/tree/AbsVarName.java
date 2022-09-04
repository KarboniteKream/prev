package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsVarName extends AbsExpr {
	public final String name;

	public AbsVarName(Position pos, String name) {
		super(pos);
		this.name = name;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}