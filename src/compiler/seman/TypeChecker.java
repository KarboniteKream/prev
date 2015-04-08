package compiler.seman;

import java.util.*;

import compiler.*;
import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.type.*;

/**
 * Preverjanje tipov.
 * 
 * @author sliva
 */
public class TypeChecker implements Visitor
{
	public void visit(AbsArrType acceptor)
	{
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, new SemArrType(acceptor.length, SymbDesc.getType(acceptor.type)));
	}

	public void visit(AbsAtomConst acceptor)
	{
		SymbDesc.setType(acceptor, new SemAtomType(acceptor.type));
	}

	public void visit(AbsAtomType acceptor)
	{
		SymbDesc.setType(acceptor, new SemAtomType(acceptor.type));
	}

	public void visit(AbsBinExpr acceptor)
	{
		switch(acceptor.oper)
		{
			case AbsBinExpr.IOR: case AbsBinExpr.AND:
			break;
		}

		// TODO: ASSIGN to variable.
	}

	public void visit(AbsComp acceptor)
	{
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.type));
	}

	public void visit(AbsCompName acceptor)
	{
	}

	public void visit(AbsDefs acceptor)
	{
		for(int i = 0; i < acceptor.numDefs(); i++)
		{
			AbsDef definition = acceptor.def(i);

			if(definition instanceof AbsTypeDef == true)
			{
				SymbDesc.setType(definition, new SemTypeName(((AbsTypeDef)definition).name));
			}
			else if(definition instanceof AbsFunDef == true)
			{
				AbsFunDef function = (AbsFunDef)definition;
				Vector<SemType> parameters = new Vector<SemType>();

				for(int j = 0; j < function.numPars(); j++)
				{
					function.par(j).accept(this);
					parameters.add(SymbDesc.getType(function.par(j)));
				}

				function.type.accept(this);
				SymbDesc.setType(function, new SemFunType(parameters, SymbDesc.getType(function.type)));
			}
		}

		// TODO: Check if types should also be finished.
		for(int i = 0; i < acceptor.numDefs(); i++)
		{
			if(acceptor.def(i) instanceof AbsVarDef == true)
			{
				AbsVarDef variable = (AbsVarDef)acceptor.def(i);
				variable.type.accept(this);
				SymbDesc.setType(variable, SymbDesc.getType(variable.type));
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

		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.expr(acceptor.numExprs() - 1)));
	}

	public void visit(AbsFor acceptor)
	{
	}

	public void visit(AbsFunCall acceptor)
	{
	}

	public void visit(AbsFunDef acceptor)
	{
		acceptor.expr.accept(this);

		SemType returnType = ((SemFunType)SymbDesc.getType(acceptor)).resultType;
		SemType expression = SymbDesc.getType(acceptor.expr);

		if(returnType.sameStructureAs(expression) == false)
		{
			Report.error(acceptor.expr.position, "Expression does not match function return type. Got " + expression + ", expected " + returnType + ".");
		}
	}

	public void visit(AbsIfThen acceptor)
	{
	}

	public void visit(AbsIfThenElse acceptor)
	{
	}

	public void visit(AbsPar acceptor)
	{
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.type));
	}

	public void visit(AbsPtrType acceptor)
	{
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, new SemPtrType(SymbDesc.getType(acceptor.type)));
	}

	public void visit(AbsRecType acceptor)
	{
		Vector<String> names = new Vector<String>();
		Vector<SemType> types = new Vector<SemType>();

		for(int i = 0; i < acceptor.numComps(); i++)
		{
			AbsComp component = acceptor.comp(i);
			component.accept(this);
			names.add(component.name);
			types.add(SymbDesc.getType(component));
		}

		SymbDesc.setType(acceptor, new SemRecType(names, types));
	}

	public void visit(AbsTypeDef acceptor)
	{
		acceptor.type.accept(this);
		((SemTypeName)SymbDesc.getType(acceptor)).setType(SymbDesc.getType(acceptor.type));
	}

	public void visit(AbsTypeName acceptor)
	{
		SymbDesc.setType(acceptor, SymbDesc.getType(SymbDesc.getNameDef(acceptor)));
	}

	public void visit(AbsUnExpr acceptor)
	{
		acceptor.expr.accept(this);

		if(acceptor.oper == AbsUnExpr.NOT && SymbDesc.getType(acceptor.expr).sameStructureAs(new SemAtomType(SemAtomType.LOG)) == false)
		{
			Report.error(acceptor.expr.position, "Expression type should be LOGICAL, got " + SymbDesc.getType(acceptor.expr).actualType() + ".");
		}
		else if((acceptor.oper == AbsUnExpr.ADD || acceptor.oper == AbsUnExpr.SUB) && SymbDesc.getType(acceptor.expr).sameStructureAs(new SemAtomType(SemAtomType.INT)) == false)
		{
			Report.error(acceptor.expr.position, "Expression type should be INTEGER, got " + SymbDesc.getType(acceptor.expr).actualType() + ".");
		}
		else if(acceptor.oper == AbsUnExpr.MEM)
		{
			SymbDesc.setType(acceptor, new SemPtrType(SymbDesc.getType(acceptor.expr)));
		}
		else if(acceptor.oper == AbsUnExpr.VAL)
		{
			if(SymbDesc.getType(acceptor.expr) instanceof SemPtrType == true)
			{
				SymbDesc.setType(acceptor, ((SemPtrType)SymbDesc.getType(acceptor.expr)).type);
			}
			else
			{
				System.out.println("NOK");
			}
		}
		else
		{
			SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.expr));
		}
	}

	public void visit(AbsVarDef acceptor) {}

	public void visit(AbsVarName acceptor)
	{
		SymbDesc.setType(acceptor, SymbDesc.getType(SymbDesc.getNameDef(acceptor)));
	}

	public void visit(AbsWhere acceptor)
	{
		acceptor.defs.accept(this);
		acceptor.expr.accept(this);
		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.expr));
	}

	public void visit(AbsWhile acceptor)
	{
	}
}
