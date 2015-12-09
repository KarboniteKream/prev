package compiler.seman;

import java.util.*;

import compiler.*;
import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.type.*;

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
		acceptor.expr1.accept(this);
		acceptor.expr2.accept(this);

		SemType type1 = SymbDesc.getType(acceptor.expr1);
		SemType type2 = SymbDesc.getType(acceptor.expr2);

		SemAtomType bool = new SemAtomType(SemAtomType.BOOLEAN);
		SemAtomType integer = new SemAtomType(SemAtomType.INT);
		SemAtomType string = new SemAtomType(SemAtomType.STR);
		SemAtomType floatType = new SemAtomType(SemAtomType.FLOAT);
		SemAtomType charType = new SemAtomType(SemAtomType.CHAR);

		// kaj pa operacije s chari? upostevaj, da je char enako kot int -> odstrani char?
		switch(acceptor.oper)
		{
			case AbsBinExpr.OR: case AbsBinExpr.AND:
				if(type1.sameStructureAs(bool) == false || type2.sameStructureAs(bool) == false)
				{
					Report.error(acceptor.position, "Expected (BOOLEAN, BOOLEAN), found (" + type1.actualType() + ", " + type2.actualType() + ").");
				}

				SymbDesc.setType(acceptor, bool);
			break;

			case AbsBinExpr.ADD: case AbsBinExpr.SUB:
			case AbsBinExpr.MUL: case AbsBinExpr.DIV:
			// podpora za MOD?
			case AbsBinExpr.MOD:
				if((type1.sameStructureAs(integer) == false && type1.sameStructureAs(floatType) == false) || (type2.sameStructureAs(integer) == false && type2.sameStructureAs(floatType) == false))
				{
					Report.error(acceptor.position, "Expected (INTEGER, INTEGER), found (" + type1.actualType() + ", " + type2.actualType() + ").");
				}

				SymbDesc.setType(acceptor, integer);
			break;

			// ali to podpiramo za floate? kako to sploh naredimo, ce imamo samo en register?
			// boolean glej samo za == in !=
			case AbsBinExpr.EQU: case AbsBinExpr.NEQ:
			case AbsBinExpr.LEQ: case AbsBinExpr.GEQ:
			case AbsBinExpr.LTH: case AbsBinExpr.GTH:
				if(type1.sameStructureAs(bool) == true)
				{
					if(type2.sameStructureAs(bool) == false)
					{
						Report.error(acceptor.expr2.position, "Expected BOOLEAN, found " + type2.actualType() + ".");
					}
				}
				// tukaj nujno tudi char
				else if(type1.sameStructureAs(integer) == true)
				{
					if(type2.sameStructureAs(integer) == false)
					{
						Report.error(acceptor.expr2.position, "Expected INTEGER, found " + type2.actualType() + ".");
					}
				}
				// else if(type1.actualType() instanceof SemPtrType == true)
				// {
				// 	if(type2.actualType() instanceof SemPtrType == false)
				// 	{
				// 		Report.error(acceptor.expr2.position, "Expected PTR, found " + type2.actualType() + ".");
				// 	}

				// 	if(type1.sameStructureAs(type2) == false)
				// 	{
				// 		Report.error(acceptor.expr2.position, "Expected " + type1.actualType() + ", found " + type2.actualType() + ".");
				// 	}
				// }
				else
				{
					Report.error(acceptor.expr1.position, "Expected { BOOLEAN, INTEGER, PTR }, found " + type1.actualType() + ".");
				}

				SymbDesc.setType(acceptor, bool);
			break;

			case AbsBinExpr.ARR:
				if(type1.actualType() instanceof SemArrType == false || type2.sameStructureAs(integer) == false)
				{
					Report.error(acceptor.position, "Expected (ARR, INTEGER), found (" + type1.actualType() + ", " + type2.actualType() + ").");
				}
				// tukaj je lahko string. NE, na tem mestu gledamo samo, ce je indeks ok. to dodaj v assign

				SymbDesc.setType(acceptor, ((SemArrType)type1.actualType()).type);
			break;

			// case AbsBinExpr.DOT:
			// 	if(type1.actualType() instanceof SemRecType == false)
			// 	{
			// 		Report.error(acceptor.expr1.position, "Expected REC, found " + type1.actualType() + ".");
			// 	}

			// 	SemRecType record = (SemRecType)type1.actualType();

			// 	for(int i = 0; i < record.getNumComps(); i++)
			// 	{
			// 		if(record.getCompName(i).equals(((AbsCompName)acceptor.expr2).name) == true)
			// 		{
			// 			SymbDesc.setType(acceptor.expr2, record.getCompType(i));
			// 			SymbDesc.setType(acceptor, record.getCompType(i));
			// 			return;
			// 		}
			// 	}

			// 	Report.error(acceptor.expr2.position, "Undefined component " + ((AbsCompName)acceptor.expr2).name + ".");
			// break;

			case AbsBinExpr.ASSIGN:
				if(type1.sameStructureAs(bool) == true && type2.sameStructureAs(bool) == false)
				{
					Report.error(acceptor.expr2.position, "Expected BOOLEAN, found " + type2.actualType() + ".");
				}
				else if(type1.sameStructureAs(integer) == true && type2.sameStructureAs(integer) == false)
				{
					Report.error(acceptor.expr2.position, "Expected INTEGER, found " + type2.actualType() + ".");
				}
				// if type1 == ARR(char) and type2 == str -> OK
				else if(type1.sameStructureAs(string) == true && type2.sameStructureAs(string) == false)
				{
					Report.error(acceptor.expr2.position, "Expected STRING, found " + type2.actualType() + ".");
				}
				else if(type1.sameStructureAs(floatType) == true && type2.sameStructureAs(floatType) == false)
				{
					Report.error(acceptor.expr2.position, "Expected FLOAT, found " + type2.actualType() + ".");
				}
				else if(type1.sameStructureAs(charType) == true && type2.sameStructureAs(charType) == false)
				{
					Report.error(acceptor.expr2.position, "Expected CHAR, found " + type2.actualType() + ".");
				}
				// else if(type1.actualType() instanceof SemPtrType == true)
				// {
				// 	if(type2.actualType() instanceof SemPtrType == false)
				// 	{
				// 		Report.error(acceptor.expr2.position, "Expected PTR, found " + type2.actualType() + ".");
				// 	}

				// 	if(type1.sameStructureAs(type2) == false)
				// 	{
				// 		Report.error(acceptor.expr2.position, "Expected " + type1.actualType() + ", found " + type2.actualType() + ".");
				// 	}
				// }
				else
				{
					// zakaj tukaj pride do napake, ce je vse ok? ali je prislo do napake, ker se hotel enaciti char, pa prej ni bil podprt?
					// TODO: odstrani v PREV
					// Report.error(acceptor.expr1.position, "Expected { BOOLEAN, INTEGER, STRING, PTR }, found " + type1.actualType() + ".");
				}

				SymbDesc.setType(acceptor, type1);
			break;
		}
	}

	public void visit(AbsComp acceptor)
	{
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.type));
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

		if(SymbDesc.getType(acceptor.cond).sameStructureAs(new SemAtomType(SemAtomType.BOOLEAN)) == true)
		{
			SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
		}
		else
		{
			Report.error(acceptor.cond.position, "Expected BOOLEAN, found " + SymbDesc.getType(acceptor.cond).actualType() + ".");
		}
	}

	public void visit(AbsExprs acceptor)
	{
		SemType type = null;

		for(int i = 0; i < acceptor.numExprs(); i++)
		{
			acceptor.expr(i).accept(this);

			if(acceptor.expr(i) instanceof AbsReturn == true)
			{
				SemType exprType = SymbDesc.getType(acceptor.expr(i));

				if(type == null)
				{
					type = exprType;
				}
				else if(exprType.sameStructureAs(type) == false)
				{
					Report.error(acceptor.expr(i).position, "Expected " + type.actualType() + ", found " + exprType.actualType() + ".");
				}
			}
		}

		if(type == null)
		{
			SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
		}
		else
		{
			SymbDesc.setType(acceptor, type);
		}
	}

	public void visit(AbsFor acceptor)
	{
		acceptor.init.accept(this);
		acceptor.cond.accept(this);
		acceptor.step.accept(this);
		acceptor.body.accept(this);

		SemType cond = SymbDesc.getType(acceptor.cond);

		if(cond.sameStructureAs(new SemAtomType(SemAtomType.VOID)) == false && cond.sameStructureAs(new SemAtomType(SemAtomType.BOOLEAN)) == false)
		{
			Report.error(acceptor.cond.position, "Expected BOOLEAN, found " + SymbDesc.getType(acceptor.cond).actualType() + ".");
		}

		SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
	}

	public void visit(AbsFunCall acceptor)
	{
		SemFunType type = (SemFunType)SymbDesc.getType(SymbDesc.getNameDef(acceptor));

		if(type.getNumPars() != acceptor.numArgs())
		{
			Report.error(acceptor.position, "Expected " + type.getNumPars() + " parameter(s), found " + acceptor.numArgs() + ".");
		}

		for(int i = 0; i < acceptor.numArgs(); i++)
		{
			acceptor.arg(i).accept(this);

			if(SymbDesc.getType(acceptor.arg(i)).sameStructureAs(type.getParType(i)) == false)
			{
				Report.error(acceptor.arg(i).position, "Expected " + type.getParType(i).actualType() + ", found " + SymbDesc.getType(acceptor.arg(i)).actualType() + ".");
			}
		}

		SymbDesc.setType(acceptor, type.resultType);
	}

	public void visit(AbsFunDef acceptor)
	{
		acceptor.type.accept(this);
		SemType funType = SymbDesc.getType(acceptor.type);

		Vector<SemType> parameters = new Vector<SemType>();
		for(int i = 0; i < acceptor.numPars(); i++)
		{
			acceptor.par(i).accept(this);
			parameters.add(SymbDesc.getType(acceptor.par(i)));
		}

		SymbDesc.setType(acceptor, new SemFunType(parameters, funType));

		acceptor.expr.accept(this);
		SemType expression = SymbDesc.getType(acceptor.expr);

		if(expression.sameStructureAs(funType) == false)
		{
			Report.error(acceptor.expr.position, "Expected " + funType.actualType() + ", found " + expression.actualType() + ".");
		}
	}

	public void visit(AbsIf acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);

		if(SymbDesc.getType(acceptor.cond).sameStructureAs(new SemAtomType(SemAtomType.BOOLEAN)) == true)
		{
			SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
		}
		else
		{
			Report.error(acceptor.cond.position, "Expected BOOLEAN, found " + SymbDesc.getType(acceptor.cond).actualType() + ".");
		}
	}

	public void visit(AbsIfElse acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);
		acceptor.elseBody.accept(this);

		if(SymbDesc.getType(acceptor.cond).sameStructureAs(new SemAtomType(SemAtomType.BOOLEAN)) == true)
		{
			SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
		}
		else
		{
			Report.error(acceptor.cond.position, "Expected BOOLEAN, found " + SymbDesc.getType(acceptor.cond).actualType() + ".");
		}
	}

	public void visit(AbsNop acceptor)
	{
		SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
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

	public void visit(AbsReturn acceptor)
	{
		acceptor.expr.accept(this);
		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.expr));
	}

	public void visit(AbsTypeDef acceptor)
	{
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, new SemTypeName(acceptor.name, SymbDesc.getType(acceptor.type)));
	}

	public void visit(AbsTypeName acceptor)
	{
		SymbDesc.setType(acceptor, SymbDesc.getType(SymbDesc.getNameDef(acceptor)));
	}

	public void visit(AbsUnExpr acceptor)
	{
		acceptor.expr.accept(this);

		if(acceptor.oper == AbsUnExpr.NOT)
		{
			if(SymbDesc.getType(acceptor.expr).sameStructureAs(new SemAtomType(SemAtomType.BOOLEAN)) == true)
			{
				SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.BOOLEAN));
			}
			else
			{
				Report.error(acceptor.expr.position, "Expected BOOLEAN, found " + SymbDesc.getType(acceptor.expr).actualType() + ".");
			}
		}
		else if(acceptor.oper == AbsUnExpr.ADD || acceptor.oper == AbsUnExpr.SUB)
		{
			if(SymbDesc.getType(acceptor.expr).sameStructureAs(new SemAtomType(SemAtomType.INT)) == true)
			{
				SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.INT));
			}
			else
			{
				Report.error(acceptor.expr.position, "Expected INTEGER, found " + SymbDesc.getType(acceptor.expr).actualType() + ".");
			}
		}
		else if(acceptor.oper == AbsUnExpr.MEM)
		{
			SymbDesc.setType(acceptor, new SemPtrType(SymbDesc.getType(acceptor.expr)));
		}
		else if(acceptor.oper == AbsUnExpr.VAL)
		{
			if(SymbDesc.getType(acceptor.expr).actualType() instanceof SemPtrType == true)
			{
				SymbDesc.setType(acceptor, ((SemPtrType)SymbDesc.getType(acceptor.expr).actualType()).type);
			}
			else
			{
				Report.error(acceptor.expr.position, "Expected PTR, found " + SymbDesc.getType(acceptor.expr).actualType() + ".");
			}
		}
	}

	public void visit(AbsVarDef acceptor)
	{
		acceptor.type.accept(this);

		if(acceptor.expr != null)
		{
			acceptor.expr.accept(this);
			SemType type1 = SymbDesc.getType(acceptor.type);
			SemType type2 = SymbDesc.getType(acceptor.expr);

			if(type1.sameStructureAs(type2) == false)
			{
				Report.error(acceptor.expr.position, "Expected " + type1.actualType() + ", found " + type2.actualType() + ".");
			}
		}

		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.type));
	}

	public void visit(AbsVarName acceptor)
	{
		SymbDesc.setType(acceptor, SymbDesc.getType(SymbDesc.getNameDef(acceptor)));
	}

	public void visit(AbsWhile acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.body.accept(this);

		if(SymbDesc.getType(acceptor.cond).sameStructureAs(new SemAtomType(SemAtomType.BOOLEAN)) == true)
		{
			SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
		}
		else
		{
			Report.error(acceptor.cond.position, "Expected BOOLEAN, found " + SymbDesc.getType(acceptor.cond).actualType() + ".");
		}
	}
}
