package compiler.seman.type;

public class SemArrType extends SemType {
	public final SemType type;
	public final long size;

	public SemArrType(long size, SemType type) {
		this.type = type;
		this.size = size;
	}

	@Override
	public boolean sameStructureAs(SemType type) {
		if (!(type.actualType() instanceof SemArrType)) {
			return false;
		}

		final SemArrType arrayType = (SemArrType) (type.actualType());
		return (arrayType.size == size) && (arrayType.type.sameStructureAs(this.type));
	}

	@Override
	public String toString() {
		return "ARR(" + size + "," + type.toString() + ")";
	}

	public long size() {
		return size * type.size();
	}
}
