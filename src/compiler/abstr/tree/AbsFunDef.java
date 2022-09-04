package compiler.abstr.tree;

import java.util.*;

import compiler.*;
import compiler.abstr.*;

public class AbsFunDef extends AbsDef {
	public final String name;
	private final AbsPar[] pars;
	public final AbsType type;
	public final AbsExpr expr;

	public AbsFunDef(Position pos, String name, Vector<AbsPar> pars, AbsType type, AbsExpr expr) {
		super(pos);

		this.name = name;
		this.pars = new AbsPar[pars.size()];
		this.type = type;
		this.expr = expr;

		for (int par = 0; par < pars.size(); par++) {
			this.pars[par] = pars.elementAt(par);
		}
	}

	public AbsPar par(int index) {
		return pars[index];
	}

	public int numPars() {
		return pars.length;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
