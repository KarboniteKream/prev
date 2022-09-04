package compiler.seman.type;

import compiler.*;

public class SemTypeName extends SemType {
	public final String name;
	private SemType type;

	public SemTypeName(String name) {
		this.name = name;
		this.type = null;
	}

	public void setType(SemType type) {
		if (this.type == null) {
			this.type = type;
		} else {
			Report.error("Internal error :: compiler.seman.type.SemTypeName.setType(SemType type)");
		}
	}

	public SemType getType() {
		if (this.type != null) {
			return this.type;
		}

		Report.error("Internal error :: compiler.seman.type.SemTypeName.getType()");
		return null;
	}

	@Override
	public SemType actualType() {
		return type.actualType();
	}

	@Override
	public boolean sameStructureAs(SemType type) {
		return this.actualType().sameStructureAs(type.actualType());
	}

	@Override
	public String toString() {
		return "TYP(" + name + ":" + (type == null ? "" : type.toString()) + ")";
	}

	public long size() {
		return type.size();
	}
}
