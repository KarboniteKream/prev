package compiler.seman;

import java.util.*;

import compiler.abstr.tree.*;
import compiler.seman.type.*;

public class SymbDesc {
	private static final HashMap<AbsTree, Integer> scope = new HashMap<>();
	private static final HashMap<AbsTree, AbsDef> nameDef = new HashMap<>();
	private static final HashMap<AbsTree, SemType> type = new HashMap<>();

	public static void setScope(AbsTree node, int nodeScope) {
		scope.put(node, nodeScope);
	}

	public static Integer getScope(AbsTree node) {
		return scope.get(node);
	}

	public static void setNameDef(AbsTree node, AbsDef def) {
		nameDef.put(node, def);
	}

	public static AbsDef getNameDef(AbsTree node) {
		return nameDef.get(node);
	}

	public static void setType(AbsTree node, SemType typ) {
		type.put(node, typ);
	}

	public static SemType getType(AbsTree node) {
		return type.get(node);
	}
}
