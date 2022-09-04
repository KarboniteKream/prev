package compiler.abstr.tree;

import java.util.*;

import compiler.*;
import compiler.abstr.*;

public class AbsRecType extends AbsType {
	private final AbsComp[] comps;

	public AbsRecType(Position pos, Vector<AbsComp> comps) {
		super(pos);

		this.comps = new AbsComp[comps.size()];
		for (int comp = 0; comp < comps.size(); comp++) {
			this.comps[comp] = comps.elementAt(comp);
		}
	}

	public AbsComp comp(int index) {
		return comps[index];
	}

	public int numComps() {
		return comps.length;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
