package compiler.seman.type;

public abstract class SemType
{
	public SemType actualType()
	{
		return this;
	}

	public abstract boolean sameStructureAs(SemType type);
	public abstract int size();
}
