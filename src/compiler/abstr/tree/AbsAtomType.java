package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsAtomType extends AbsType
{
	// Pazi, da se stevilke povsod ujemajo.
	public static final int BOOL = 0;
	public static final int INT = 1;
	// public static final int STR = 2;
	public static final int FLOAT = 3;
	public static final int CHAR = 4;
	public static final int VOID = 5;

	public final int type;

	public AbsAtomType(Position pos, int type)
	{
		super(pos);
		this.type = type;
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }

}
