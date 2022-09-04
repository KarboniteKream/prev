package compiler.abstr.tree;

import java.util.*;

import compiler.*;
import compiler.abstr.*;

public class AbsDefs extends AbsTree {
	private final AbsDef[] defs;

	public AbsDefs(Position pos, Vector<AbsDef> defs) {
		super(pos);

		this.defs = new AbsDef[defs.size()];
		for (int def = 0; def < defs.size(); def++) {
			this.defs[def] = defs.elementAt(def);
		}
	}

	public AbsDef def(int index) {
		return defs[index];
	}

	public int numDefs() {
		return defs.length;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
