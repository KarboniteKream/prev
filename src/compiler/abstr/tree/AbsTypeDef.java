package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsTypeDef extends AbsDef {

	public final AbsType type;

	public AbsTypeDef(Position pos, String name, AbsType type) {
		super(pos, name);
		this.type = type;
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }

}
