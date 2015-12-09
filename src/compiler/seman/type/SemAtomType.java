package compiler.seman.type;

import compiler.*;

public class SemAtomType extends SemType {

	public static final int BOOLEAN = 0;
	public static final int INT = 1;
	// remove string
	public static final int STR = 2;
	public static final int FLOAT = 3;
	public static final int CHAR = 4;
	public static final int VOID = 5;

	public final int type;

	public SemAtomType(int type)
	{
		this.type = type;
	}

	@Override
	public boolean sameStructureAs(SemType type)
	{
		if (type.actualType() instanceof SemAtomType) {
			SemAtomType atomType = (SemAtomType) (type.actualType());
			return this.type == atomType.type;
		} else
			return false;
	}

	@Override
	public String toString()
	{
		switch(type)
		{
			case BOOLEAN: return "BOOLEAN";
			case INT: return "INTEGER";
			case STR: return "STRING";
			case VOID: return "VOID";
			case CHAR: return "CHAR";
			case FLOAT: return "FLOAT";
		}

		Report.error("Internal error :: compiler.seman.type.SemAtomType.toString()");
		return "";
	}

	public int size()
	{
		if(type == FLOAT)
		{
			return 6;
		}

		return 3;
	}
}
