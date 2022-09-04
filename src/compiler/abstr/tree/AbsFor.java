package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsFor extends AbsExpr {
	public final AbsExpr count;
	public final AbsExpr lo;
	public final AbsExpr hi;
	public final AbsExpr step;
	public final AbsExpr body;

	public AbsFor(Position pos, AbsExpr count, AbsExpr lo, AbsExpr hi, AbsExpr step, AbsExpr body) {
		super(pos);
		this.count = count;
		this.lo = lo;
		this.hi = hi;
		this.step = step;
		this.body = body;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
