package compiler.abstr.tree;

import java.util.*;

import compiler.*;
import compiler.abstr.*;

public class AbsFunCall extends AbsExpr {
	public final String name;
	private final AbsExpr[] args;

	public AbsFunCall(Position pos, String name, Vector<AbsExpr> args) {
		super(pos);

		this.name = name;
		this.args = new AbsExpr[args.size()];

		for (int arg = 0; arg < args.size(); arg++) {
			this.args[arg] = args.elementAt(arg);
		}
	}

	public AbsExpr arg(int index) {
		return args[index];
	}

	public int numArgs() {
		return args.length;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}