package compiler.frames;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.*;
import compiler.seman.type.*;

public class FrmEvaluator implements Visitor {
	private static int level = 1;
	private static FrmFrame frame = null;

	public void visit(AbsArrType acceptor) {
		acceptor.type.accept(this);
	}

	public void visit(AbsAtomConst acceptor) {}
	public void visit(AbsAtomType acceptor) {}

	public void visit(AbsBinExpr acceptor) {
		acceptor.expr1.accept(this);
		acceptor.expr2.accept(this);
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
	}

	public void visit(AbsFor acceptor) {
		acceptor.lo.accept(this);
		acceptor.hi.accept(this);
		acceptor.step.accept(this);
		acceptor.body.accept(this);
	}

	public void visit(AbsFunCall acceptor) {
		long size = 8;
		final long returnSize = ((SemFunType) SymbDesc.getType(SymbDesc.getNameDef(acceptor))).resultType.size();

		for (int i = 0; i < acceptor.numArgs(); i++) {
			size += SymbDesc.getType(acceptor.arg(i)).size();
		}

		size = Math.max(returnSize, size);
		frame.sizeArgs = Math.max(size, frame.sizeArgs);
	}

	public void visit(AbsFunDef acceptor) {
		final FrmFrame temp = frame;
		frame = new FrmFrame(acceptor, level++);

		for (int i = 0; i < acceptor.numPars(); i++) {
			acceptor.par(i).accept(this);
		}

		acceptor.type.accept(this);
		acceptor.expr.accept(this);

		FrmDesc.setFrame(acceptor, frame);
		level--;
		frame = temp;
	}

	public void visit(AbsIfThen acceptor) {
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);
	}

	public void visit(AbsIfThenElse acceptor) {
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);
		acceptor.elseBody.accept(this);
	}

	public void visit(AbsPar acceptor) {
		FrmDesc.setAccess(acceptor, new FrmParAccess(acceptor, frame));
		acceptor.type.accept(this);
	}

	public void visit(AbsPtrType acceptor) {
		acceptor.type.accept(this);
	}

	public void visit(AbsRecType acceptor) {
		long offset = 0;

		for (int i = 0; i < acceptor.numComps(); i++) {
			final AbsComp component = acceptor.comp(i);
			FrmDesc.setAccess(component, new FrmCmpAccess(component, offset));
			offset += SymbDesc.getType(component).size();
			component.type.accept(this);
		}
	}

	public void visit(AbsTypeDef acceptor) {
		acceptor.type.accept(this);
	}

	public void visit(AbsTypeName acceptor) {}

	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);
	}

	public void visit(AbsVarDef acceptor) {
		if (frame == null) {
			FrmDesc.setAccess(acceptor, new FrmVarAccess(acceptor));
		} else {
			final FrmLocAccess variable = new FrmLocAccess(acceptor, frame);
			FrmDesc.setAccess(acceptor, variable);
			frame.locVars.add(variable);
		}

		acceptor.type.accept(this);
	}

	public void visit(AbsVarName acceptor) {}

	public void visit(AbsWhere acceptor) {
		acceptor.defs.accept(this);
		acceptor.expr.accept(this);
	}

	public void visit(AbsWhile acceptor) {
		acceptor.cond.accept(this);
		acceptor.body.accept(this);
	}
}
