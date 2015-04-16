package compiler.frames;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.*;

public class FrmEvaluator implements Visitor
{
	private static int level = 1;
	private static FrmFrame frame = null;

	public void visit(AbsArrType acceptor)
	{
	}

	public void visit(AbsAtomConst acceptor)
	{
	}

	public void visit(AbsAtomType acceptor)
	{
	}

	public void visit(AbsBinExpr acceptor)
	{
	}

	public void visit(AbsComp acceptor)
	{
	}

	public void visit(AbsCompName acceptor)
	{
	}

	public void visit(AbsDefs acceptor)
	{
		for(int i = 0; i < acceptor.numDefs(); i++)
		{
			acceptor.def(i).accept(this);
		}
	}

	public void visit(AbsExprs acceptor)
	{
		for(int i = 0; i < acceptor.numExprs(); i++)
		{
			acceptor.expr(i).accept(this);
		}
	}

	public void visit(AbsFor acceptor)
	{
	}

	public void visit(AbsFunCall acceptor)
	{
	}

	public void visit(AbsFunDef acceptor)
	{
		FrmFrame temp = frame;
		frame = new FrmFrame(acceptor, level);

		for(int i = 0; i < acceptor.numPars(); i++)
		{
			acceptor.par(i).accept(this);
		}

		acceptor.expr.accept(this);

		FrmDesc.setFrame(acceptor, frame);
		frame = temp;
	}

	public void visit(AbsIfThen acceptor)
	{
	}

	public void visit(AbsIfThenElse acceptor)
	{
	}

	public void visit(AbsPar acceptor)
	{
		FrmDesc.setAccess(acceptor, new FrmParAccess(acceptor, frame));
	}

	public void visit(AbsPtrType acceptor)
	{
	}

	public void visit(AbsRecType acceptor)
	{
	}

	public void visit(AbsTypeDef acceptor)
	{
	}

	public void visit(AbsTypeName acceptor)
	{
	}

	public void visit(AbsUnExpr acceptor)
	{
		// TODO: Operation?
		acceptor.expr.accept(this);
	}

	public void visit(AbsVarDef acceptor)
	{
		if(frame == null)
		{
			FrmDesc.setAccess(acceptor, new FrmVarAccess(acceptor));
		}
		else
		{
			FrmLocAccess variable = new FrmLocAccess(acceptor, frame);
			FrmDesc.setAccess(acceptor, variable);
			frame.locVars.add(variable);
		}

		// TODO: Type?
	}

	public void visit(AbsVarName acceptor) {}

	public void visit(AbsWhere acceptor)
	{
		acceptor.defs.accept(this);
		acceptor.expr.accept(this);
	}

	public void visit(AbsWhile acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.body.accept(this);
	}
}
