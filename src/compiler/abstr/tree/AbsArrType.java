package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsArrType extends AbsType {
	public final long length;
	public final AbsType type;

	public AbsArrType(Position pos, long length, AbsType type) {
		super(pos);
		this.length = length;
		this.type = type;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
