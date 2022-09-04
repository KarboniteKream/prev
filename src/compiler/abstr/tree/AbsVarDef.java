package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsVarDef extends AbsDef {
	public final String name;
	public final AbsType type;

	public AbsVarDef(Position pos, String name, AbsType type) {
		super(pos);
		this.name = name;
		this.type = type;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
