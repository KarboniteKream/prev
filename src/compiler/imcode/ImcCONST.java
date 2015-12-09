package compiler.imcode;

import compiler.*;

public class ImcCONST extends ImcExpr
{
	public int value;

	public ImcCONST(int value)
	{
		this.value = value;
	}

	@Override
	public void dump(int indent)
	{
		Report.dump(indent, "CONST value=" + value);
	}

	@Override
	public ImcESEQ linear()
	{
		return new ImcESEQ(new ImcSEQ(), this);
	}
}
