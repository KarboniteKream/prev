package compiler.seman.type;

import java.util.*;

public class SemRecType extends SemType {
	private final String[] compNames;
	private final SemType[] compTypes;

	public SemRecType(Vector<String> compNames, Vector<SemType> compTypes) {
		this.compNames = new String[compNames.size()];
		for (int comp = 0; comp < compNames.size(); comp++) {
			this.compNames[comp] = compNames.elementAt(comp);
		}

		this.compTypes = new SemType[compTypes.size()];
		for (int comp = 0; comp < compTypes.size(); comp++) {
			this.compTypes[comp] = compTypes.elementAt(comp);
		}
	}

	public int getNumComps() {
		return compTypes.length;
	}

	public String getCompName(int index) {
		return compNames[index];
	}

	public SemType getCompType(int index) {
		return compTypes[index];
	}

	@Override
	public boolean sameStructureAs(SemType type) {
		if (!(type.actualType() instanceof SemRecType)) {
			return false;
		}

		final SemRecType recordType = (SemRecType) (type.actualType());
		if (this.getNumComps() != recordType.getNumComps()) {
			return false;
		}

		for (int i = 0; i < getNumComps(); i++) {
			if (!this.getCompType(i).sameStructureAs(recordType.getCompType(i))) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		String str = "REC(";

		for (int comp = 0; comp < compTypes.length; comp++) {
			str += (comp > 0 ? "," : "") + compTypes[comp].toString();
		}

		str += ")";
		return str;
	}

	public long size() {
		long size = 0;

		for (SemType compType : compTypes) {
			size += compType.size();
		}

		return size;
	}
}
