package compiler.frames;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.*;
import compiler.seman.type.*;

public class FrmEvaluator implements Visitor
{
	// frame rabim zaradi globalnih spremenljivk, zato je to boljse kot level
	private static FrmFrame frame = null;

	public void visit(AbsArrType acceptor)
	{
		acceptor.type.accept(this);
	}

	public void visit(AbsAtomConst acceptor) {}
	public void visit(AbsAtomType acceptor) {}

	public void visit(AbsBinExpr acceptor)
	{
		acceptor.expr1.accept(this);
		acceptor.expr2.accept(this);
	}

	public void visit(AbsComp acceptor) {}
	public void visit(AbsCompName acceptor) {}

	public void visit(AbsDefs acceptor)
	{
		for(int i = 0; i < acceptor.numDefs(); i++)
		{
			acceptor.def(i).accept(this);
		}
	}

	public void visit(AbsDoWhile acceptor)
	{
		acceptor.body.accept(this);
		acceptor.cond.accept(this);
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
		acceptor.init.accept(this);
		acceptor.cond.accept(this);
		acceptor.body.accept(this);
	}

	public void visit(AbsFunCall acceptor)
	{
		// zacetna velikost je 0 ali 3, ker ne rabim FP.
		// popravi velikost podatkovnih tipov na 3 in 6
		// getNameDef() in nato iz frame poberi podatke. frame mora obstajati!
		int size = 0;
		int returnSize = ((SemFunType)SymbDesc.getType(SymbDesc.getNameDef(acceptor))).resultType.size();

		for(int i = 0; i < acceptor.numArgs(); i++)
		{
			size += SymbDesc.getType(acceptor.arg(i)).size();
		}

		size = returnSize > size ? returnSize : size;
		frame.sizeArgs = size > frame.sizeArgs ? size : frame.sizeArgs;
	}

	public void visit(AbsFunDef acceptor)
	{
		// podpora za prototipe funkcij?
		frame = new FrmFrame(acceptor);

		for(int i = 0; i < acceptor.numPars(); i++)
		{
			acceptor.par(i).accept(this);
		}

		acceptor.type.accept(this);
		acceptor.expr.accept(this);

		FrmDesc.setFrame(acceptor, frame);
		frame = null;
	}

	public void visit(AbsIf acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);
	}

	public void visit(AbsIfElse acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);
		acceptor.elseBody.accept(this);
	}

	public void visit(AbsNop acceptor) {}

	public void visit(AbsPar acceptor)
	{
		FrmDesc.setAccess(acceptor, new FrmParAccess(acceptor, frame));
		acceptor.type.accept(this);
	}

	public void visit(AbsPtrType acceptor)
	{
		acceptor.type.accept(this);
	}

	public void visit(AbsRecType acceptor)
	{
		int offset = 0;

		for(int i = 0; i < acceptor.numComps(); i++)
		{
			AbsComp component = acceptor.comp(i);
			FrmDesc.setAccess(component, new FrmCmpAccess(component, offset));
			offset += SymbDesc.getType(component).size();
			component.type.accept(this);
		}
	}

	public void visit(AbsReturn acceptor)
	{
		acceptor.expr.accept(this);
	}

	public void visit(AbsTypeDef acceptor)
	{
		acceptor.type.accept(this);
	}

	public void visit(AbsTypeName acceptor) {}

	public void visit(AbsUnExpr acceptor)
	{
		acceptor.expr.accept(this);
	}

	public void visit(AbsVarDef acceptor)
	{
		// kaj pa stringi, ki se inicializirajo ob vsakem klicu?
		// ce imamo string konstanto, potem je tudi to globalna spremenljivka
		if(frame == null)
		{
			// rename to global?
			FrmDesc.setAccess(acceptor, new FrmVarAccess(acceptor));
		}
		else
		{
			FrmLocAccess variable = new FrmLocAccess(acceptor, frame);
			FrmDesc.setAccess(acceptor, variable);
			frame.locVars.add(variable);
		}

		acceptor.type.accept(this);
		
		if(acceptor.expr != null)
		{
			acceptor.expr.accept(this);
		}
	}

	public void visit(AbsVarName acceptor) {}

	public void visit(AbsWhile acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.body.accept(this);
	}
}
