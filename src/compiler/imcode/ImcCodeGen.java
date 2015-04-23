package compiler.imcode;

import java.util.*;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.frames.*;
import compiler.seman.*;
import compiler.seman.type.*;

public class ImcCodeGen implements Visitor
{
	public LinkedList<ImcChunk> chunks;
	private HashMap<AbsTree, ImcCode> imcode;
	private FrmFrame frame;

	public ImcCodeGen()
	{
		chunks = new LinkedList<ImcChunk>();
		imcode = new HashMap<AbsTree, ImcCode>();
		frame = null;
	}

	public void visit(AbsArrType acceptor) {}

	public void visit(AbsAtomConst acceptor)
	{
		if(acceptor.type == AbsAtomConst.INT)
		{
			imcode.put(acceptor, new ImcCONST(Integer.parseInt(acceptor.value)));
		}
		else if(acceptor.type == AbsAtomConst.LOG)
		{
			imcode.put(acceptor, new ImcCONST(acceptor.value.equals("true") == true ? 1 : 0));
		}
		else if(acceptor.type == AbsAtomConst.STR)
		{
			FrmLabel string = FrmLabel.newLabel();
			chunks.add(new ImcDataChunk(string, (acceptor.value.length() - 2) * 8));
			imcode.put(acceptor, new ImcMEM(new ImcNAME(string)));
		}
	}

	public void visit(AbsAtomType acceptor) {}

	public void visit(AbsBinExpr acceptor)
	{
		acceptor.expr1.accept(this);
		acceptor.expr2.accept(this);

		ImcExpr expr1 = (ImcExpr)imcode.get(acceptor.expr1);
		ImcExpr expr2 = (ImcExpr)imcode.get(acceptor.expr2);

		if(acceptor.oper == AbsBinExpr.ASSIGN)
		{
			ImcSEQ move = new ImcSEQ();
			move.stmts.add(new ImcMOVE(expr1, expr2));
			imcode.put(acceptor, new ImcESEQ(move, expr1));
		}
		else if(acceptor.oper == AbsBinExpr.ARR)
		{
			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, ((ImcMEM)expr1).expr, new ImcBINOP(ImcBINOP.MUL, expr2, new ImcCONST(((SemArrType)SymbDesc.getType(acceptor.expr1)).type.size())))));
		}
		else if(acceptor.oper == AbsBinExpr.DOT)
		{
			SemRecType record = (SemRecType)SymbDesc.getType(SymbDesc.getNameDef(acceptor.expr1));
			int offset = 0;

			if(record == null)
			{
				record = (SemRecType)SymbDesc.getType(acceptor.expr1);
			}

			for(int i = 0; i < record.getNumComps(); i++)
			{
				if(record.getCompName(i).equals(((AbsCompName)acceptor.expr2).name) == true)
				{
					break;
				}

				offset += record.getCompType(i).size();
			}

			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, ((ImcMEM)expr1).expr, new ImcCONST(offset))));
		}
		else if(acceptor.oper == AbsBinExpr.MOD)
		{
			imcode.put(acceptor, new ImcBINOP(ImcBINOP.SUB, expr1, new ImcBINOP(ImcBINOP.MUL, expr2, new ImcBINOP(ImcBINOP.DIV, expr1, expr2))));
		}
		else
		{
			imcode.put(acceptor, new ImcBINOP(acceptor.oper, expr1, expr2));
		}
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

	public void visit(AbsExprs acceptor)
	{
		ImcSEQ expressions = new ImcSEQ();

		for(int i = 0; i < acceptor.numExprs() - 1; i++)
		{
			acceptor.expr(i).accept(this);
			ImcCode expression = imcode.get(acceptor.expr(i));
			expressions.stmts.add(expression instanceof ImcStmt == true ? (ImcStmt)expression : new ImcEXP((ImcExpr)expression));
		}

		acceptor.expr(acceptor.numExprs() - 1).accept(this);
		imcode.put(acceptor, new ImcESEQ(expressions, (ImcExpr)imcode.get(acceptor.expr(acceptor.numExprs() - 1))));
	}

	public void visit(AbsFor acceptor)
	{
		acceptor.count.accept(this);
		acceptor.lo.accept(this);
		acceptor.hi.accept(this);
		acceptor.step.accept(this);
		acceptor.body.accept(this);

		ImcSEQ expressions = new ImcSEQ();
		FrmLabel startLabel = FrmLabel.newLabel();
		FrmLabel bodyLabel = FrmLabel.newLabel();
		FrmLabel endLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcMOVE(new ImcMEM((ImcExpr)imcode.get(acceptor.count)), (ImcExpr)imcode.get(acceptor.lo)));
		expressions.stmts.add(new ImcLABEL(startLabel));
		expressions.stmts.add(new ImcCJUMP(new ImcBINOP(ImcBINOP.LTH, (ImcExpr)imcode.get(acceptor.count), (ImcExpr)imcode.get(acceptor.hi)), bodyLabel, endLabel));
		expressions.stmts.add(new ImcLABEL(bodyLabel));
		ImcCode expression = imcode.get(acceptor.body);
		expressions.stmts.add(expression instanceof ImcStmt == true ? (ImcStmt)expression : new ImcEXP((ImcExpr)expression));
		expressions.stmts.add(new ImcMOVE(new ImcMEM((ImcExpr)imcode.get(acceptor.count)), new ImcBINOP(ImcBINOP.ADD, new ImcMEM((ImcExpr)imcode.get(acceptor.count)), (ImcExpr)imcode.get(acceptor.step))));
		expressions.stmts.add(new ImcJUMP(startLabel));
		expressions.stmts.add(new ImcLABEL(endLabel));

		imcode.put(acceptor, expressions);
	}

	public void visit(AbsFunCall acceptor)
	{
		FrmFrame function = FrmDesc.getFrame(SymbDesc.getNameDef(acceptor));
		ImcCALL call = new ImcCALL(function.label);
		ImcExpr location = new ImcTEMP(frame.FP);

		for(int i = 0; i <= frame.level - function.level; i++)
		{
			location = new ImcMEM(location);
		}

		call.args.add(location);

		for(int i = 0; i < acceptor.numArgs(); i++)
		{
			acceptor.arg(i).accept(this);
			call.args.add((ImcExpr)imcode.get(acceptor.arg(i)));
		}

		imcode.put(acceptor, call);
	}

	public void visit(AbsFunDef acceptor)
	{
		FrmFrame temp = frame;
		frame = FrmDesc.getFrame(acceptor);
		acceptor.expr.accept(this);
		chunks.add(new ImcCodeChunk(frame, new ImcMOVE(new ImcTEMP(frame.RV), (ImcExpr)imcode.get(acceptor.expr))));
		frame = temp;
	}

	public void visit(AbsIfThen acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);

		ImcSEQ expressions = new ImcSEQ();
		FrmLabel trueLabel = FrmLabel.newLabel();
		FrmLabel falseLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcCJUMP((ImcExpr)imcode.get(acceptor.cond), trueLabel, falseLabel));
		expressions.stmts.add(new ImcLABEL(trueLabel));
		ImcCode expression = imcode.get(acceptor.thenBody);
		expressions.stmts.add(expression instanceof ImcStmt == true ? (ImcStmt)expression : new ImcEXP((ImcExpr)expression));
		expressions.stmts.add(new ImcLABEL(falseLabel));

		imcode.put(acceptor, expressions);
	}

	public void visit(AbsIfThenElse acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);
		acceptor.elseBody.accept(this);

		ImcSEQ expressions = new ImcSEQ();
		FrmLabel trueLabel = FrmLabel.newLabel();
		FrmLabel falseLabel = FrmLabel.newLabel();
		FrmLabel endLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcCJUMP((ImcExpr)imcode.get(acceptor.cond), trueLabel, falseLabel));
		expressions.stmts.add(new ImcLABEL(trueLabel));
		ImcCode thenExpression = imcode.get(acceptor.thenBody);
		expressions.stmts.add(thenExpression instanceof ImcStmt == true ? (ImcStmt)thenExpression : new ImcEXP((ImcExpr)thenExpression));
		expressions.stmts.add(new ImcJUMP(endLabel));
		expressions.stmts.add(new ImcLABEL(falseLabel));
		ImcCode elseExpression = imcode.get(acceptor.elseBody);
		expressions.stmts.add(elseExpression instanceof ImcStmt == true ? (ImcStmt)elseExpression : new ImcEXP((ImcExpr)elseExpression));
		expressions.stmts.add(new ImcLABEL(endLabel));

		imcode.put(acceptor, expressions);
	}

	public void visit(AbsPar acceptor) {}
	public void visit(AbsPtrType acceptor) {}
	public void visit(AbsRecType acceptor) {}
	public void visit(AbsTypeDef acceptor) {}
	public void visit(AbsTypeName acceptor) {}

	public void visit(AbsUnExpr acceptor)
	{
		acceptor.expr.accept(this);

		if(acceptor.oper == AbsUnExpr.ADD)
		{
			imcode.put(acceptor, imcode.get(acceptor.expr));
		}
		else if(acceptor.oper == AbsUnExpr.SUB)
		{
			imcode.put(acceptor, new ImcBINOP(ImcBINOP.SUB, new ImcCONST(0), (ImcExpr)imcode.get(acceptor.expr)));
		}
		else if(acceptor.oper == AbsUnExpr.MEM)
		{
			imcode.put(acceptor, ((ImcMEM)imcode.get(acceptor.expr)).expr);
		}
		else if(acceptor.oper == AbsUnExpr.VAL)
		{
			imcode.put(acceptor, new ImcMEM((ImcExpr)imcode.get(acceptor.expr)));
		}
		else if(acceptor.oper == AbsUnExpr.NOT)
		{
			imcode.put(acceptor, new ImcBINOP(ImcBINOP.EQU, (ImcExpr)imcode.get(acceptor.expr), new ImcCONST(0)));
		}
	}

	public void visit(AbsVarDef acceptor)
	{
		if(frame == null)
		{
			chunks.add(new ImcDataChunk(((FrmVarAccess)FrmDesc.getAccess(acceptor)).label, SymbDesc.getType(acceptor).size()));
		}
	}

	public void visit(AbsVarName acceptor)
	{
		FrmAccess access = FrmDesc.getAccess(SymbDesc.getNameDef(acceptor));

		if(access instanceof FrmLocAccess == true)
		{
			FrmLocAccess variable = (FrmLocAccess)access;

			ImcExpr location = new ImcTEMP(frame.FP);
			for(int i = 0; i < frame.level - variable.frame.level; i++)
			{
				location = new ImcMEM(location);
			}

			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, (ImcExpr)location, new ImcCONST(variable.offset))));
		}
		else if(access instanceof FrmParAccess == true)
		{
			FrmParAccess parameter = (FrmParAccess)access;

			ImcExpr location = new ImcTEMP(frame.FP);
			for(int i = 0; i < frame.level - parameter.frame.level; i++)
			{
				location = new ImcMEM(location);
			}

			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, (ImcExpr)location, new ImcCONST(parameter.offset))));
		}
		else if(access instanceof FrmVarAccess == true)
		{
			imcode.put(acceptor, new ImcMEM(new ImcNAME(((FrmVarAccess)access).label)));
		}
	}

	public void visit(AbsWhere acceptor)
	{
		acceptor.defs.accept(this);
		acceptor.expr.accept(this);

		imcode.put(acceptor, imcode.get(acceptor.expr));
	}

	public void visit(AbsWhile acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.body.accept(this);

		ImcSEQ expressions = new ImcSEQ();
		ImcCode expression = imcode.get(acceptor.body);
		FrmLabel startLabel = FrmLabel.newLabel();
		FrmLabel bodyLabel = FrmLabel.newLabel();
		FrmLabel endLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcLABEL(startLabel));
		expressions.stmts.add(new ImcCJUMP((ImcExpr)imcode.get(acceptor.cond), bodyLabel, endLabel));
		expressions.stmts.add(new ImcLABEL(bodyLabel));
		expressions.stmts.add(expression instanceof ImcStmt == true ? (ImcStmt)expression : new ImcEXP((ImcExpr)expression));
		expressions.stmts.add(new ImcJUMP(startLabel));
		expressions.stmts.add(new ImcLABEL(endLabel));

		imcode.put(acceptor, expressions);
	}
}
