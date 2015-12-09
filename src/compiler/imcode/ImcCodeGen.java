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
		else if(acceptor.type == AbsAtomConst.BOOL)
		{
			imcode.put(acceptor, new ImcCONST(acceptor.value.equals("true") == true ? 1 : 0));
		}
		else if(acceptor.type == AbsAtomConst.STR)
		{
			// TODO
			FrmLabel string = FrmLabel.newLabel();
			chunks.add(new ImcDataChunk(string, ImcDataChunk.BYTE, null, "\"" + acceptor.value.substring(1, acceptor.value.length() - 1) + "\",0"));
			imcode.put(acceptor, new ImcNAME(string));
		}
		else if(acceptor.type == AbsAtomConst.FLOAT)
		{
			// TODO: float
		}
		else if(acceptor.type == AbsAtomConst.CHAR)
		{
			imcode.put(acceptor, new ImcCONST(acceptor.value.charAt(1)));
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
			ImcTEMP temp = new ImcTEMP(new FrmTemp());
			move.stmts.add(new ImcMOVE(temp, ((ImcMEM)expr1).expr));
			move.stmts.add(new ImcMOVE(new ImcMEM(temp), expr2));
			imcode.put(acceptor, new ImcESEQ(move, new ImcMEM(temp)));
		}
		else if(acceptor.oper == AbsBinExpr.ARR)
		{
			if(expr1 instanceof ImcESEQ == true)
			{
				expr1 = ((ImcESEQ)expr1).expr;
			}

			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, ((ImcMEM)expr1).expr, new ImcBINOP(ImcBINOP.MUL, expr2, new ImcCONST(((SemArrType)SymbDesc.getType(acceptor.expr1).actualType()).type.size())))));
		}
		// else if(acceptor.oper == AbsBinExpr.DOT)
		// {
		// 	SemType type = SymbDesc.getType(SymbDesc.getNameDef(acceptor.expr1));

		// 	if(type == null)
		// 	{
		// 		type = SymbDesc.getType(acceptor.expr1);
		// 	}

		// 	SemRecType record = (SemRecType)type.actualType();
		// 	int offset = 0;

		// 	for(int i = 0; i < record.getNumComps(); i++)
		// 	{
		// 		if(record.getCompName(i).equals(((AbsCompName)acceptor.expr2).name) == true)
		// 		{
		// 			break;
		// 		}

		// 		offset += record.getCompType(i).size();
		// 	}

		// 	if(expr1 instanceof ImcESEQ == true)
		// 	{
		// 		expr1 = ((ImcESEQ)expr1).expr;
		// 	}

		// 	imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, ((ImcMEM)expr1).expr, new ImcCONST(offset))));
		// }
		else if(acceptor.oper == AbsBinExpr.MOD)
		{
			ImcSEQ expressions = new ImcSEQ();

			ImcTEMP left = new ImcTEMP(new FrmTemp());
			ImcTEMP right = new ImcTEMP(new FrmTemp());

			expressions.stmts.add(new ImcMOVE(left, expr1));
			expressions.stmts.add(new ImcMOVE(right, expr2));

			imcode.put(acceptor, new ImcESEQ(expressions, new ImcBINOP(ImcBINOP.SUB, left, new ImcBINOP(ImcBINOP.MUL, new ImcBINOP(ImcBINOP.DIV, left, right), right))));
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

	public void visit(AbsDoWhile acceptor)
	{
		acceptor.body.accept(this);
		acceptor.cond.accept(this);

		ImcSEQ expressions = new ImcSEQ();
		ImcCode expression = imcode.get(acceptor.body);
		FrmLabel startLabel = FrmLabel.newLabel();
		FrmLabel endLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcLABEL(startLabel));
		expressions.stmts.add(expression instanceof ImcStmt == true ? (ImcStmt)expression : new ImcEXP((ImcExpr)expression));
		expressions.stmts.add(new ImcCJUMP((ImcExpr)imcode.get(acceptor.cond), startLabel, endLabel));
		expressions.stmts.add(new ImcLABEL(endLabel));

		imcode.put(acceptor, expressions);
	}

	public void visit(AbsExprs acceptor)
	{
		for(int i = 0; i < acceptor.numExprs(); i++)
		{
			acceptor.expr(i).accept(this);
		}

		if(acceptor.numExprs() == 0)
		{
			// TODO: ce je vektor prazen, potem nop?
			imcode.put(acceptor, new ImcCONST(888));
		}
		else if(acceptor.numExprs() == 1)
		{
			imcode.put(acceptor, imcode.get(acceptor.expr(0)));
		}
		else
		{
			ImcSEQ expressions = new ImcSEQ();

			for(int i = 0; i < acceptor.numExprs(); i++)
			{
				ImcCode expression = imcode.get(acceptor.expr(i));
				expressions.stmts.add(expression instanceof ImcStmt == true ? (ImcStmt)expression : new ImcEXP((ImcExpr)expression));
			}

			imcode.put(acceptor, expressions);
		}
	}

	public void visit(AbsFor acceptor)
	{
		acceptor.init.accept(this);
		acceptor.cond.accept(this);
		acceptor.step.accept(this);
		acceptor.body.accept(this);

		ImcSEQ expressions = new ImcSEQ();
		FrmLabel condLabel = FrmLabel.newLabel();
		FrmLabel bodyLabel = FrmLabel.newLabel();
		FrmLabel endLabel = FrmLabel.newLabel();

		ImcCode initExpression = imcode.get(acceptor.init);
		expressions.stmts.add(initExpression instanceof ImcStmt == true ? (ImcStmt)initExpression : new ImcEXP((ImcExpr)initExpression));
		expressions.stmts.add(new ImcLABEL(condLabel));
		expressions.stmts.add(new ImcCJUMP((ImcExpr)imcode.get(acceptor.cond), bodyLabel, endLabel));
		expressions.stmts.add(new ImcLABEL(bodyLabel));
		ImcCode bodyExpression = imcode.get(acceptor.body);
		expressions.stmts.add(bodyExpression instanceof ImcStmt == true ? (ImcStmt)bodyExpression : new ImcEXP((ImcExpr)bodyExpression));
		ImcCode stepExpression = imcode.get(acceptor.step);
		expressions.stmts.add(stepExpression instanceof ImcStmt == true ? (ImcStmt)stepExpression : new ImcEXP((ImcExpr)stepExpression));
		expressions.stmts.add(new ImcJUMP(condLabel));
		expressions.stmts.add(new ImcLABEL(endLabel));

		imcode.put(acceptor, expressions);
	}

	public void visit(AbsFunCall acceptor)
	{
		FrmFrame function = FrmDesc.getFrame(SymbDesc.getNameDef(acceptor));
		ImcCALL call = new ImcCALL(function.label);

		for(int i = 0; i < acceptor.numArgs(); i++)
		{
			acceptor.arg(i).accept(this);
			call.args.add((ImcExpr)imcode.get(acceptor.arg(i)));
		}

		imcode.put(acceptor, call);
		// tukaj dobimo move(temp, call). kaj vrne call? ali to handlamo v asmcode? PREV generira enako
	}

	public void visit(AbsFunDef acceptor)
	{
		frame = FrmDesc.getFrame(acceptor);
		acceptor.expr.accept(this);
		ImcCode expression = imcode.get(acceptor.expr);
		// TODO: NOP, ce ni nobenega expressiona? kako je z linearizacijo? To ima Exprs cez?
		// ali je tukaj lahko ImcExpr oz obratno? ImcExpr se pojavi pri praznem exprs, ki trenutno samo const888 nastavi
		chunks.add(new ImcCodeChunk(frame, expression instanceof ImcStmt == true ? (ImcStmt)expression : new ImcEXP((ImcExpr)expression)));
		frame = null;
	}

	public void visit(AbsIf acceptor)
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

	public void visit(AbsIfElse acceptor)
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

	public void visit(AbsNop acceptor)
	{
		// TODO
		imcode.put(acceptor, new ImcCONST(999));
	}

	public void visit(AbsPar acceptor) {}
	public void visit(AbsPtrType acceptor) {}
	public void visit(AbsRecType acceptor) {}

	public void visit(AbsReturn acceptor)
	{
		// preveri ce je absnop ALI naredi imcnop
		if(acceptor.expr instanceof AbsNop == false)
		{
			acceptor.expr.accept(this);
			ImcSEQ statements = new ImcSEQ();
			// To se ne dela? ali RV na stack shranimo sele v epilogu?
			statements.stmts.add(new ImcMOVE(new ImcTEMP(frame.RV), (ImcExpr)imcode.get(acceptor.expr)));
			statements.stmts.add(new ImcJUMP(frame.endLabel));
			imcode.put(acceptor, statements);
		}
		else
		{
			imcode.put(acceptor, new ImcJUMP(frame.endLabel));
		}
	}

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
		// naredi inc in dec, uporabi seq
	}

	public void visit(AbsVarDef acceptor)
	{
		if(acceptor.expr != null)
		{
			acceptor.expr.accept(this);
		}

		// odstrani, da ne bo isti if?
		// globalno
		if(frame == null)
		{
			// datachunk mora vsebovati kodo na zacetku programa ki inicializira spremenljivko
			chunks.add(new ImcDataChunk(((FrmVarAccess)FrmDesc.getAccess(acceptor)).label, SymbDesc.getType(acceptor).size()));
			FrmAccess acc = FrmDesc.getAccess(acceptor);
			// TODO expr, add to datachunk
			System.out.print(acc.getClass().getName());
			System.out.println(" " + acceptor.name);
			imcode.put(acceptor, new ImcCONST(123));
		}
		// drugi if!
		// lokalno
		else if(acceptor.expr != null)
		{
			// kako je pa s stringi?
			ImcTEMP temp = new ImcTEMP(new FrmTemp());
			ImcSEQ move = new ImcSEQ();
			move.stmts.add(new ImcMOVE(temp, new ImcBINOP(ImcBINOP.ADD, (ImcExpr)new ImcTEMP(frame.FP), new ImcCONST(((FrmLocAccess)FrmDesc.getAccess(acceptor)).offset))));
			move.stmts.add(new ImcMOVE(new ImcMEM(temp), (ImcExpr)imcode.get(acceptor.expr)));
			imcode.put(acceptor, new ImcESEQ(move, new ImcMEM(temp)));
		}
		else
		{
			// TODO: ImcNop, ce je null?
			imcode.put(acceptor, new ImcCONST(999));
		}
	}

	public void visit(AbsVarName acceptor)
	{
		FrmAccess access = FrmDesc.getAccess(SymbDesc.getNameDef(acceptor));

		if(access instanceof FrmLocAccess == true)
		{
			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, (ImcExpr)new ImcTEMP(frame.FP), new ImcCONST(((FrmLocAccess)access).offset))));
		}
		else if(access instanceof FrmParAccess == true)
		{
			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, (ImcExpr)new ImcTEMP(frame.FP), new ImcCONST(((FrmParAccess)access).offset))));
		}
		else if(access instanceof FrmVarAccess == true)
		{
			imcode.put(acceptor, new ImcMEM(new ImcNAME(((FrmVarAccess)access).label)));
		}
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
