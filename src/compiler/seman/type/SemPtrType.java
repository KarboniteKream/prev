package compiler.seman.type;

public class SemPtrType extends SemType {
	public final SemType type;

	public SemPtrType(SemType type) {
		this.type = type;
	}

	@Override
	public boolean sameStructureAs(SemType type) {
		if (!(type.actualType() instanceof SemPtrType)) {
			return false;
		}

		final SemPtrType pointerType = (SemPtrType) (type.actualType());
		return (pointerType.type.sameStructureAs(this.type));
	}

	@Override
	public String toString() {
		return "PTR(" + type.toString() + ")";
	}

	public long size() {
		return 8;
	}
}
