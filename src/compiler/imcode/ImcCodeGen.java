package compiler.imcode;

import java.util.*;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.frames.*;
import compiler.seman.*;

public class ImcCodeGen implements Visitor
{
	public LinkedList<ImcChunk> chunks;
	private HashMap<AbsTree, ImcCode> code;

	private int level = 1;

	public ImcCodeGen()
	{
		chunks = new LinkedList<ImcChunk>();
		code = new HashMap<AbsTree, ImcCode>();
	}

	public void visit(AbsArrType acceptor) {}

	public void visit(AbsAtomConst acceptor)
	{
		if(acceptor.type == AbsAtomConst.INT)
		{
			code.put(acceptor, new ImcCONST(Integer.parseInt(acceptor.value)));
		}
		else if(acceptor.type == AbsAtomConst.LOG)
		{
			code.put(acceptor, new ImcCONST(acceptor.value.equals("true") == true ? 1 : 0));
		}
		else if(acceptor.type == AbsAtomConst.STR)
		{
			// TODO
		}
	}

	public void visit(AbsAtomType acceptor) {}

	public void visit(AbsBinExpr acceptor)
	{
		acceptor.expr1.accept(this);
		acceptor.expr2.accept(this);

		// FIXME: Operator IDs don't match.
		code.put(acceptor, new ImcBINOP(0, (ImcExpr)code.get(acceptor.expr1), (ImcExpr)code.get(acceptor.expr2)));
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
		ImcSEQ expressions = new ImcSEQ();

		for(int i = 0; i < acceptor.numExprs(); i++)
		{
			acceptor.expr(i).accept(this);
		}

		for(int i = 0; i < acceptor.numExprs() - 1; i++)
		{
			ImcCode expression = code.get(acceptor.expr(i));
			expressions.stmts.add(expression instanceof ImcStmt == true ? (ImcStmt)expression : new ImcEXP((ImcExpr)expression));
		}

		code.put(acceptor, new ImcESEQ(expressions, (ImcExpr)code.get(acceptor.expr(acceptor.numExprs() - 1))));
	}

	public void visit(AbsFor acceptor)
	{
	}

	public void visit(AbsFunCall acceptor)
	{
	}

	public void visit(AbsFunDef acceptor)
	{
		level++;
		acceptor.expr.accept(this);
		level--;

		FrmFrame frame = FrmDesc.getFrame(acceptor);
		ImcCode expression = code.get(acceptor.expr);
		ImcCodeChunk chunk = null;

		chunks.add(new ImcCodeChunk(frame, new ImcMOVE(new ImcMEM(new ImcMEM(new ImcTEMP(frame.RV))), (ImcExpr)expression)));
	}

	public void visit(AbsIfThen acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);

		ImcSEQ expressions = new ImcSEQ();
		FrmLabel trueLabel = FrmLabel.newLabel();
		FrmLabel falseLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcCJUMP((ImcExpr)code.get(acceptor.cond), trueLabel, falseLabel));
		expressions.stmts.add(new ImcLABEL(trueLabel));
		expressions.stmts.add(new ImcEXP((ImcExpr)code.get(acceptor.thenBody)));
		expressions.stmts.add(new ImcLABEL(falseLabel));

		code.put(acceptor, expressions);
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

		expressions.stmts.add(new ImcCJUMP((ImcExpr)code.get(acceptor.cond), trueLabel, falseLabel));
		expressions.stmts.add(new ImcLABEL(trueLabel));
		expressions.stmts.add(new ImcEXP((ImcExpr)code.get(acceptor.thenBody)));
		expressions.stmts.add(new ImcJUMP(endLabel));
		expressions.stmts.add(new ImcLABEL(falseLabel));
		expressions.stmts.add(new ImcEXP((ImcExpr)code.get(acceptor.elseBody)));
		expressions.stmts.add(new ImcLABEL(endLabel));

		code.put(acceptor, expressions);
	}

	public void visit(AbsPar acceptor)
	{
	}

	public void visit(AbsPtrType acceptor) {}
	public void visit(AbsRecType acceptor) {}
	public void visit(AbsTypeDef acceptor) {}

	public void visit(AbsTypeName acceptor)
	{
	}

	public void visit(AbsUnExpr acceptor)
	{
	}

	public void visit(AbsVarDef acceptor)
	{
		if(level == 1)
		{
			chunks.add(new ImcDataChunk(FrmLabel.newLabel(acceptor.name), SymbDesc.getType(acceptor).size()));
		}
	}

	public void visit(AbsVarName acceptor)
	{
	}

	public void visit(AbsWhere acceptor)
	{
		acceptor.defs.accept(this);
		acceptor.expr.accept(this);
	}

	public void visit(AbsWhile acceptor)
	{
		acceptor.cond.accept(this);
		acceptor.body.accept(this);

		ImcSEQ expressions = new ImcSEQ();
		ImcCode expression = code.get(acceptor.body);
		FrmLabel startLabel = FrmLabel.newLabel();
		FrmLabel bodyLabel = FrmLabel.newLabel();
		FrmLabel endLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcLABEL(startLabel));
		expressions.stmts.add(new ImcCJUMP((ImcExpr)code.get(acceptor.cond), bodyLabel, endLabel));
		expressions.stmts.add(new ImcLABEL(bodyLabel));
		expressions.stmts.add(expression instanceof ImcStmt == true ? (ImcStmt)expression : new ImcEXP((ImcExpr)expression));
		expressions.stmts.add(new ImcJUMP(startLabel));
		expressions.stmts.add(new ImcLABEL(endLabel));

		code.put(acceptor, expressions);
	}
}
