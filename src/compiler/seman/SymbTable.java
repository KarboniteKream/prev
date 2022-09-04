package compiler.seman;

import java.util.*;

import compiler.*;
import compiler.abstr.tree.*;

public class SymbTable {
	private static final HashMap<String, LinkedList<AbsDef>> mapping = new HashMap<>();
	private static int scope = 0;

	public static void newScope() {
		scope++;
	}

	public static void oldScope() {
		final LinkedList<String> allNames = new LinkedList<>(mapping.keySet());

		for (String name : allNames) {
			try {
				SymbTable.del(name);
			} catch (SemIllegalDeleteException ignored) {
			}
		}

		scope--;
	}

	public static void ins(String name, AbsDef newDef) throws SemIllegalInsertException {
		LinkedList<AbsDef> allNameDefs = mapping.get(name);

		if (allNameDefs == null) {
			allNameDefs = new LinkedList<>();
			allNameDefs.addFirst(newDef);
			SymbDesc.setScope(newDef, scope);
			mapping.put(name, allNameDefs);
			return;
		}

		if ((allNameDefs.size() == 0) || (SymbDesc.getScope(allNameDefs.getFirst()) == null)) {
			Thread.dumpStack();
			Report.error("Internal error.");
			return;
		}

		if (SymbDesc.getScope(allNameDefs.getFirst()) == scope) {
			throw new SemIllegalInsertException();
		}

		allNameDefs.addFirst(newDef);
		SymbDesc.setScope(newDef, scope);
	}

	public static void del(String name) throws SemIllegalDeleteException {
		final LinkedList<AbsDef> allNameDefs = mapping.get(name);

		if (allNameDefs == null) {
			throw new SemIllegalDeleteException();
		}

		if ((allNameDefs.size() == 0) || (SymbDesc.getScope(allNameDefs.getFirst()) == null)) {
			Thread.dumpStack();
			Report.error("Internal error.");
			return;
		}

		if (SymbDesc.getScope(allNameDefs.getFirst()) < scope) {
			throw new SemIllegalDeleteException();
		}

		allNameDefs.removeFirst();

		if (allNameDefs.size() == 0) {
			mapping.remove(name);
		}
	}

	public static AbsDef fnd(String name) {
		final LinkedList<AbsDef> allNameDefs = mapping.get(name);

		if (allNameDefs == null || allNameDefs.size() == 0) {
			return null;
		}

		return allNameDefs.getFirst();
	}
}
