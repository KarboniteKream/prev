package compiler.seman;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.Report;

public class NameChecker implements Visitor
{
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

	public void visit(AbsComp acceptor)
	{
		acceptor.type.accept(this);
	}

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
		SymbTable.newScope();

		for(int i = 0; i < acceptor.numExprs(); i++)
		{
			acceptor.expr(i).accept(this);
		}

		SymbTable.oldScope();
	}

	public void visit(AbsFor acceptor)
	{
		acceptor.init.accept(this);
		acceptor.cond.accept(this);
		acceptor.step.accept(this);
		acceptor.body.accept(this);
	}

	public void visit(AbsFunCall acceptor)
	{
		AbsDef function = SymbTable.fnd(acceptor.name);

		if(function == null || function instanceof AbsFunDef == false)
		{
			Report.error(acceptor.position, "Undefined function '" + acceptor.name + "'.");
		}

		SymbDesc.setNameDef(acceptor, function);

		for(int i = 0; i < acceptor.numArgs(); i++)
		{
			acceptor.arg(i).accept(this);
		}
	}

	public void visit(AbsFunDef acceptor)
	{
		acceptor.type.accept(this);

		try
		{
			SymbTable.ins(acceptor.name, acceptor);
		}
		catch(SemIllegalInsertException __)
		{
			Report.error(acceptor.position, "Redefinition of function '" + acceptor.name + "'.");
		}

		SymbTable.newScope();

		for(int i = 0; i < acceptor.numPars(); i++)
		{
			acceptor.par(i).accept(this);
		}

		acceptor.expr.accept(this);
		SymbTable.oldScope();
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
		acceptor.type.accept(this);

		// Kdaj pride tle do ponovne definicije?
		try
		{
			SymbTable.ins(acceptor.name, acceptor);
		}
		catch(SemIllegalInsertException e)
		{
			Report.error(acceptor.position, "Redefinition of variable '" + acceptor.name + "'.");
		}
	}

	public void visit(AbsPtrType acceptor)
	{
		// TODO
		acceptor.type.accept(this);
	}

	// TODO
	public void visit(AbsRecType acceptor)
	{
		for(int i = acceptor.numComps() - 1; i > 0; i--)
		{
			AbsComp component = acceptor.comp(i);

			for(int j = i - 1; j >= 0; j--)
			{
				if(component.name.equals(acceptor.comp(j).name))
				{
					Report.error(component.position, "Redefinition of component " + component.name + ".");
				}
			}
		}

		for(int i = 0; i < acceptor.numComps(); i++)
		{
			acceptor.comp(i).accept(this);
		}
	}

	public void visit(AbsReturn acceptor)
	{
		acceptor.expr.accept(this);
	}

	public void visit(AbsTypeDef acceptor)
	{
		acceptor.type.accept(this);

		try
		{
			SymbTable.ins(acceptor.name, acceptor);
		}
		catch(SemIllegalInsertException __)
		{
			Report.error(acceptor.position, "Redefinition of type '" + acceptor.name + "'.");
		}
	}

	public void visit(AbsTypeName acceptor)
	{
		AbsDef type = SymbTable.fnd(acceptor.name);

		if(type == null || type instanceof AbsTypeDef == false)
		{
			Report.error(acceptor.position, "Undefined type '" + acceptor.name + "'.");
		}

		SymbDesc.setNameDef(acceptor, type);
	}

	public void visit(AbsUnExpr acceptor)
	{
		acceptor.expr.accept(this);
	}

	public void visit(AbsVarDef acceptor)
	{
		acceptor.type.accept(this);

		if(acceptor.expr != null)
		{
			acceptor.expr.accept(this);
		}

		try
		{
			SymbTable.ins(acceptor.name, acceptor);
		}
		catch(SemIllegalInsertException __)
		{
			Report.error(acceptor.position, "Redefinition of variable '" + acceptor.name + "'.");
		}
	}

	public void visit(AbsVarName acceptor)
	{
		AbsDef variable = SymbTable.fnd(acceptor.name);

		if(variable == null || (variable instanceof AbsVarDef == false && variable instanceof AbsPar == false))
		{
			Report.error(acceptor.position, "Undefined variable '" + acceptor.name + "'.");
		}

		SymbDesc.setNameDef(acceptor, variable);
	}

	public void visit(AbsWhile acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.body.accept(this);
	}
}
