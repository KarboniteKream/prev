package compiler.seman.type;

import java.util.*;

public class SemFunType extends SemType
{
	private final SemType parTypes[];
	public final SemType resultType;

	public SemFunType(Vector<SemType> parTypes, SemType resultType)
	{
		this.parTypes = new SemType[parTypes.size()];
		for (int par = 0; par < parTypes.size(); par++)
			this.parTypes[par] = parTypes.elementAt(par);
		this.resultType = resultType;
	}

	public int getNumPars()
	{
		return parTypes.length;
	}

	public SemType getParType(int index)
	{
		return parTypes[index];
	}

	@Override
	public boolean sameStructureAs(SemType type) {
		if (type.actualType() instanceof SemFunType) {
			SemFunType funType = (SemFunType) (type.actualType());
			if (this.getNumPars() != funType.getNumPars())
				return false;
			for (int par = 0; par < getNumPars(); par++)
				if (!this.getParType(par).sameStructureAs(
						funType.getParType(par)))
					return false;
			if (!this.resultType.sameStructureAs(funType.resultType))
				return false;
			return true;
		} else
			return false;
	}

	@Override
	public String toString() {
		String str = "";
		str += "FUN(";
		for (int par = 0; par < parTypes.length; par++)
			str += (par > 0 ? "," : "") + parTypes[par].toString();
		str += ":" + resultType.toString();
		str += ")";
		return str;
	}

	public int size()
	{
		return resultType.size();
	}
}
