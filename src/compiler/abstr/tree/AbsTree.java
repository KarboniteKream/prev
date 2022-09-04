package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public abstract class AbsTree {
	public final Position position;

	public AbsTree(Position pos) {
		this.position = pos;
	}

	public abstract void accept(Visitor visitor);
}
