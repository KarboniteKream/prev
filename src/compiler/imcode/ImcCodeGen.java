package compiler.imcode;

import java.util.*;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.frames.*;
import compiler.seman.*;
import compiler.seman.type.*;

public class ImcCodeGen implements Visitor {
	public final LinkedList<ImcChunk> chunks;

	private final HashMap<AbsTree, ImcCode> imcode;
	private FrmFrame frame;

	public ImcCodeGen() {
		chunks = new LinkedList<>();
		imcode = new HashMap<>();
		frame = null;
	}

	public void visit(AbsArrType acceptor) {}

	public void visit(AbsAtomConst acceptor) {
		if (acceptor.type == AbsAtomConst.INT) {
			imcode.put(acceptor, new ImcCONST(Long.parseLong(acceptor.value)));
		} else if (acceptor.type == AbsAtomConst.LOG) {
			imcode.put(acceptor, new ImcCONST(acceptor.value.equals("true") ? 1L : 0L));
		} else if (acceptor.type == AbsAtomConst.STR) {
			final FrmLabel string = FrmLabel.newLabel();
			chunks.add(new ImcDataChunk(string, ImcDataChunk.BYTE, "\"" + acceptor.value.substring(1, acceptor.value.length() - 1) + "\",0"));
			imcode.put(acceptor, new ImcNAME(string));
		}
	}

	public void visit(AbsAtomType acceptor) {}

	public void visit(AbsBinExpr acceptor) {
		acceptor.expr1.accept(this);
		acceptor.expr2.accept(this);

		ImcExpr expr1 = (ImcExpr) imcode.get(acceptor.expr1);
		ImcExpr expr2 = (ImcExpr) imcode.get(acceptor.expr2);

		if (acceptor.oper == AbsBinExpr.ASSIGN) {
			final ImcSEQ move = new ImcSEQ();
			final ImcTEMP temp = new ImcTEMP(new FrmTemp());
			move.stmts.add(new ImcMOVE(temp, ((ImcMEM) expr1).expr));
			move.stmts.add(new ImcMOVE(new ImcMEM(temp), expr2));
			imcode.put(acceptor, new ImcESEQ(move, new ImcMEM(temp)));
		} else if (acceptor.oper == AbsBinExpr.ARR) {
			if (expr1 instanceof ImcESEQ) {
				expr1 = ((ImcESEQ) expr1).expr;
			}

			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, ((ImcMEM) expr1).expr, new ImcBINOP(ImcBINOP.MUL, expr2, new ImcCONST(((SemArrType) SymbDesc.getType(acceptor.expr1).actualType()).type.size())))));
		} else if (acceptor.oper == AbsBinExpr.DOT) {
			SemType type = SymbDesc.getType(SymbDesc.getNameDef(acceptor.expr1));

			if (type == null) {
				type = SymbDesc.getType(acceptor.expr1);
			}

			final SemRecType record = (SemRecType) type.actualType();
			long offset = 0;

			for (int i = 0; i < record.getNumComps(); i++) {
				if (record.getCompName(i).equals(((AbsCompName) acceptor.expr2).name)) {
					break;
				}

				offset += record.getCompType(i).size();
			}

			if (expr1 instanceof ImcESEQ) {
				expr1 = ((ImcESEQ) expr1).expr;
			}

			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, ((ImcMEM) expr1).expr, new ImcCONST(offset))));
		} else if (acceptor.oper == AbsBinExpr.MOD) {
			final ImcSEQ expressions = new ImcSEQ();

			final ImcTEMP left = new ImcTEMP(new FrmTemp());
			final ImcTEMP right = new ImcTEMP(new FrmTemp());

			expressions.stmts.add(new ImcMOVE(left, expr1));
			expressions.stmts.add(new ImcMOVE(right, expr2));

			imcode.put(acceptor, new ImcESEQ(expressions, new ImcBINOP(ImcBINOP.SUB, left, new ImcBINOP(ImcBINOP.MUL, new ImcBINOP(ImcBINOP.DIV, left, right), right))));
		} else {
			imcode.put(acceptor, new ImcBINOP(acceptor.oper, expr1, expr2));
		}
	}

	public void visit(AbsComp acceptor) {}
	public void visit(AbsCompName acceptor) {}

	public void visit(AbsDefs acceptor) {
		for (int i = 0; i < acceptor.numDefs(); i++) {
			acceptor.def(i).accept(this);
		}
	}

	public void visit(AbsExprs acceptor) {
		for (int i = 0; i < acceptor.numExprs(); i++) {
			acceptor.expr(i).accept(this);
		}

		if (acceptor.numExprs() == 1) {
			imcode.put(acceptor, imcode.get(acceptor.expr(0)));
			return;
		}

		final ImcSEQ expressions = new ImcSEQ();

		for (int i = 0; i < acceptor.numExprs() - 1; i++) {
			ImcCode expression = imcode.get(acceptor.expr(i));
			expressions.stmts.add(expression instanceof ImcStmt ? (ImcStmt) expression : new ImcEXP((ImcExpr) expression));
		}

		if (imcode.get(acceptor.expr(acceptor.numExprs() - 1)) instanceof ImcExpr) {
			imcode.put(acceptor, new ImcESEQ(expressions, (ImcExpr) imcode.get(acceptor.expr(acceptor.numExprs() - 1))));
		} else {
			expressions.stmts.add((ImcStmt) imcode.get(acceptor.expr(acceptor.numExprs() - 1)));
			imcode.put(acceptor, expressions);
		}
	}

	public void visit(AbsFor acceptor) {
		acceptor.count.accept(this);
		acceptor.lo.accept(this);
		acceptor.hi.accept(this);
		acceptor.step.accept(this);
		acceptor.body.accept(this);

		final ImcSEQ expressions = new ImcSEQ();
		final FrmLabel condLabel = FrmLabel.newLabel();
		final FrmLabel lthLabel = FrmLabel.newLabel();
		final FrmLabel gthLabel = FrmLabel.newLabel();
		final FrmLabel bodyLabel = FrmLabel.newLabel();
		final FrmLabel endLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcMOVE((ImcExpr) imcode.get(acceptor.count), (ImcExpr) imcode.get(acceptor.lo)));
		expressions.stmts.add(new ImcLABEL(condLabel));
		expressions.stmts.add(new ImcCJUMP(new ImcBINOP(ImcBINOP.GEQ, (ImcExpr) imcode.get(acceptor.step), new ImcCONST(0L)), lthLabel, gthLabel));
		expressions.stmts.add(new ImcLABEL(lthLabel));
		expressions.stmts.add(new ImcCJUMP(new ImcBINOP(ImcBINOP.LTH, (ImcExpr) imcode.get(acceptor.count), (ImcExpr) imcode.get(acceptor.hi)), bodyLabel, endLabel));
		expressions.stmts.add(new ImcLABEL(gthLabel));
		expressions.stmts.add(new ImcCJUMP(new ImcBINOP(ImcBINOP.GTH, (ImcExpr) imcode.get(acceptor.count), (ImcExpr) imcode.get(acceptor.hi)), bodyLabel, endLabel));
		expressions.stmts.add(new ImcLABEL(bodyLabel));
		final ImcCode expression = imcode.get(acceptor.body);
		expressions.stmts.add(expression instanceof ImcStmt ? (ImcStmt) expression : new ImcEXP((ImcExpr) expression));
		expressions.stmts.add(new ImcMOVE((ImcExpr) imcode.get(acceptor.count), new ImcBINOP(ImcBINOP.ADD, (ImcExpr) imcode.get(acceptor.count), (ImcExpr) imcode.get(acceptor.step))));
		expressions.stmts.add(new ImcJUMP(condLabel));
		expressions.stmts.add(new ImcLABEL(endLabel));

		imcode.put(acceptor, expressions);
	}

	public void visit(AbsFunCall acceptor) {
		final FrmFrame function = FrmDesc.getFrame(SymbDesc.getNameDef(acceptor));
		final ImcCALL call = new ImcCALL(function.label);
		ImcExpr location = new ImcTEMP(frame.FP);

		for (int i = 0; i <= frame.level - function.level; i++) {
			location = new ImcMEM(location);
		}

		call.args.add(location);

		for (int i = 0; i < acceptor.numArgs(); i++) {
			acceptor.arg(i).accept(this);
			call.args.add((ImcExpr) imcode.get(acceptor.arg(i)));
		}

		imcode.put(acceptor, call);
	}

	public void visit(AbsFunDef acceptor) {
		final FrmFrame temp = frame;
		frame = FrmDesc.getFrame(acceptor);
		acceptor.expr.accept(this);
		chunks.add(new ImcCodeChunk(frame, new ImcMOVE(new ImcTEMP(frame.RV), (ImcExpr) imcode.get(acceptor.expr))));
		frame = temp;
	}

	public void visit(AbsIfThen acceptor) {
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);

		final ImcSEQ expressions = new ImcSEQ();
		final FrmLabel trueLabel = FrmLabel.newLabel();
		final FrmLabel falseLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcCJUMP((ImcExpr) imcode.get(acceptor.cond), trueLabel, falseLabel));
		expressions.stmts.add(new ImcLABEL(trueLabel));
		ImcCode expression = imcode.get(acceptor.thenBody);
		expressions.stmts.add(expression instanceof ImcStmt ? (ImcStmt) expression : new ImcEXP((ImcExpr) expression));
		expressions.stmts.add(new ImcLABEL(falseLabel));

		imcode.put(acceptor, expressions);
	}

	public void visit(AbsIfThenElse acceptor) {
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);
		acceptor.elseBody.accept(this);

		final ImcSEQ expressions = new ImcSEQ();
		final FrmLabel trueLabel = FrmLabel.newLabel();
		final FrmLabel falseLabel = FrmLabel.newLabel();
		final FrmLabel endLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcCJUMP((ImcExpr) imcode.get(acceptor.cond), trueLabel, falseLabel));
		expressions.stmts.add(new ImcLABEL(trueLabel));
		final ImcCode thenExpression = imcode.get(acceptor.thenBody);
		expressions.stmts.add(thenExpression instanceof ImcStmt ? (ImcStmt) thenExpression : new ImcEXP((ImcExpr) thenExpression));
		expressions.stmts.add(new ImcJUMP(endLabel));
		expressions.stmts.add(new ImcLABEL(falseLabel));
		final ImcCode elseExpression = imcode.get(acceptor.elseBody);
		expressions.stmts.add(elseExpression instanceof ImcStmt ? (ImcStmt) elseExpression : new ImcEXP((ImcExpr) elseExpression));
		expressions.stmts.add(new ImcLABEL(endLabel));

		imcode.put(acceptor, expressions);
	}

	public void visit(AbsPar acceptor) {}
	public void visit(AbsPtrType acceptor) {}
	public void visit(AbsRecType acceptor) {}
	public void visit(AbsTypeDef acceptor) {}
	public void visit(AbsTypeName acceptor) {}

	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);

		if (acceptor.oper == AbsUnExpr.ADD) {
			imcode.put(acceptor, imcode.get(acceptor.expr));
		} else if (acceptor.oper == AbsUnExpr.SUB) {
			imcode.put(acceptor, new ImcBINOP(ImcBINOP.SUB, new ImcCONST(0L), (ImcExpr) imcode.get(acceptor.expr)));
		} else if (acceptor.oper == AbsUnExpr.MEM) {
			imcode.put(acceptor, ((ImcMEM) imcode.get(acceptor.expr)).expr);
		} else if (acceptor.oper == AbsUnExpr.VAL) {
			imcode.put(acceptor, new ImcMEM((ImcExpr) imcode.get(acceptor.expr)));
		} else if (acceptor.oper == AbsUnExpr.NOT) {
			imcode.put(acceptor, new ImcBINOP(ImcBINOP.EQU, (ImcExpr) imcode.get(acceptor.expr), new ImcCONST(0L)));
		}
	}

	public void visit(AbsVarDef acceptor) {
		if (frame == null) {
			chunks.add(new ImcDataChunk(((FrmVarAccess) FrmDesc.getAccess(acceptor)).label, SymbDesc.getType(acceptor).size()));
		}
	}

	public void visit(AbsVarName acceptor) {
		final FrmAccess access = FrmDesc.getAccess(SymbDesc.getNameDef(acceptor));

		if (access instanceof FrmLocAccess) {
			final FrmLocAccess variable = (FrmLocAccess) access;

			ImcExpr location = new ImcTEMP(frame.FP);
			for (int i = 0; i < frame.level - variable.frame.level; i++) {
				location = new ImcMEM(location);
			}

			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, location, new ImcCONST(variable.offset))));
		} else if (access instanceof FrmParAccess) {
			final FrmParAccess parameter = (FrmParAccess) access;

			ImcExpr location = new ImcTEMP(frame.FP);
			for (int i = 0; i < frame.level - parameter.frame.level; i++) {
				location = new ImcMEM(location);
			}

			imcode.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, location, new ImcCONST(parameter.offset))));
		} else if (access instanceof FrmVarAccess) {
			imcode.put(acceptor, new ImcMEM(new ImcNAME(((FrmVarAccess) access).label)));
		}
	}

	public void visit(AbsWhere acceptor) {
		acceptor.defs.accept(this);
		acceptor.expr.accept(this);

		imcode.put(acceptor, imcode.get(acceptor.expr));
	}

	public void visit(AbsWhile acceptor) {
		acceptor.cond.accept(this);
		acceptor.body.accept(this);

		final ImcSEQ expressions = new ImcSEQ();
		final ImcCode expression = imcode.get(acceptor.body);
		final FrmLabel startLabel = FrmLabel.newLabel();
		final FrmLabel bodyLabel = FrmLabel.newLabel();
		final FrmLabel endLabel = FrmLabel.newLabel();

		expressions.stmts.add(new ImcLABEL(startLabel));
		expressions.stmts.add(new ImcCJUMP((ImcExpr) imcode.get(acceptor.cond), bodyLabel, endLabel));
		expressions.stmts.add(new ImcLABEL(bodyLabel));
		expressions.stmts.add(expression instanceof ImcStmt ? (ImcStmt) expression : new ImcEXP((ImcExpr) expression));
		expressions.stmts.add(new ImcJUMP(startLabel));
		expressions.stmts.add(new ImcLABEL(endLabel));

		imcode.put(acceptor, expressions);
	}
}
