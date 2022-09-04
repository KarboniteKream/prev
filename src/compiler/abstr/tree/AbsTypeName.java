package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsTypeName extends AbsType {
	public final String name;

	public AbsTypeName(Position pos, String name) {
		super(pos);
		this.name = name;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
