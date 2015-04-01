package compiler.seman;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.Report;

/**
 * Preverjanje in razresevanje imen (razen imen komponent).
 * 
 * @author sliva
 */
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

	public void visit(AbsCompName acceptor)
	{
	}

	public void visit(AbsDefs acceptor)
	{
		for(int i = 0; i < acceptor.numDefs(); i++)
		{
			AbsDef definition = acceptor.def(i);

			if(definition instanceof AbsTypeDef)
			{
				AbsTypeDef type = (AbsTypeDef)definition;

				try
				{
					SymbTable.ins(type.name, type);
				}
				catch(SemIllegalInsertException e)
				{
					Report.error(type.position, "Redefinition of type '" + type.name + "'.");
				}
			}
			else if(definition instanceof AbsFunDef)
			{
				AbsFunDef function = (AbsFunDef)definition;

				try
				{
					SymbTable.ins(function.name, function);
				}
				catch(SemIllegalInsertException e)
				{
					Report.error(function.position, "Redefinition of function '" + function.name + "'.");
				}
			}
			else if(definition instanceof AbsVarDef)
			{
				AbsVarDef variable = (AbsVarDef)definition;

				try
				{
					SymbTable.ins(variable.name, variable);
				}
				catch(SemIllegalInsertException e)
				{
					Report.error(variable.position, "Redefinition of variable '" + variable.name + "'.");
				}
			}
		}

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
		acceptor.count.accept(this);
		acceptor.lo.accept(this);
		acceptor.hi.accept(this);
		acceptor.step.accept(this);
		acceptor.body.accept(this);
	}

	public void visit(AbsFunCall acceptor)
	{
		AbsDef function = SymbTable.fnd(acceptor.name);

		if(function == null)
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
		SymbTable.newScope();

		for(int i = 0; i < acceptor.numPars(); i++)
		{
			acceptor.par(i).accept(this);
		}

		acceptor.type.accept(this);
		acceptor.expr.accept(this);
		SymbTable.oldScope();
	}

	public void visit(AbsIfThen acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);
	}

	public void visit(AbsIfThenElse acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);
		acceptor.elseBody.accept(this);
	}

	public void visit(AbsPar acceptor)
	{
		try
		{
			SymbTable.ins(acceptor.name, acceptor);
			acceptor.type.accept(this);
		}
		catch(SemIllegalInsertException e)
		{
			Report.error(acceptor.position, "Redefinition of variable '" + acceptor.name + "'.");
		}
	}

	public void visit(AbsPtrType acceptor)
	{
		acceptor.type.accept(this);
	}

	public void visit(AbsRecType acceptor)
	{
		for(int i = 0; i < acceptor.numComps(); i++)
		{
			acceptor.comp(i).accept(this);
		}
	}

	public void visit(AbsTypeDef acceptor)
	{
		acceptor.type.accept(this);
	}

	public void visit(AbsTypeName acceptor)
	{
		AbsDef identifier = SymbTable.fnd(acceptor.name);
		
		if(identifier == null)
		{
			Report.error(acceptor.position, "Undefined type '" + acceptor.name + "'.");
		}

		SymbDesc.setNameDef(acceptor, identifier);
	}

	public void visit(AbsUnExpr acceptor)
	{
		acceptor.expr.accept(this);
	}

	public void visit(AbsVarDef acceptor)
	{
		acceptor.type.accept(this);
	}

	public void visit(AbsVarName acceptor)
	{
		AbsDef variable = SymbTable.fnd(acceptor.name);

		if(variable == null)
		{
			Report.error(acceptor.position, "Undefined variable '" + acceptor.name + "'.");
		}

		SymbDesc.setNameDef(acceptor, variable);
	}

	public void visit(AbsWhere acceptor)
	{
		SymbTable.newScope();
		acceptor.defs.accept(this);
		acceptor.expr.accept(this);
		SymbTable.oldScope();
	}

	public void visit(AbsWhile acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.body.accept(this);
	}
}
