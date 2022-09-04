package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsPtrType extends AbsType {
	public final AbsType type;

	public AbsPtrType(Position pos, AbsType type) {
		super(pos);
		this.type = type;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
