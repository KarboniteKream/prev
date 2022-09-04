package compiler.seman.type;

import compiler.*;

public class SemAtomType extends SemType {
	public static final int LOG = 0;
	public static final int INT = 1;
	public static final int STR = 2;
	public static final int VOID = 3;

	public final int type;

	public SemAtomType(int type) {
		this.type = type;
	}

	@Override
	public boolean sameStructureAs(SemType type) {
		if (!(type.actualType() instanceof SemAtomType)) {
			return false;
		}

		final SemAtomType atomType = (SemAtomType) type.actualType();
		return this.type == atomType.type;
	}

	@Override
	public String toString() {
		switch (type) {
			case LOG: return "LOGICAL";
			case INT: return "INTEGER";
			case STR: return "STRING";
			case VOID: return "VOID";
		}

		Report.error("Internal error :: compiler.seman.type.SemAtomType.toString()");
		return "";
	}

	public long size() {
		return 8;
	}
}
