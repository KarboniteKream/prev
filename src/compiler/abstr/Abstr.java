package compiler.abstr;

import compiler.*;
import compiler.abstr.tree.*;

public class Abstr implements Visitor
{
	private boolean dump;

	public Abstr(boolean dump)
	{
		this.dump = dump;
	}

	public void dump(AbsTree tree)
	{
		if (! dump) return;
		if (Report.dumpFile() == null) return;
		indent = 0;
		tree.accept(this);
	}

	private int indent;

	public void visit(AbsArrType arrType) {
		Report.dump(indent, "AbsArrType " + arrType.position.toString() + ":");
		Report.dump(indent + 2, "[" + arrType.length + "]");
		indent += 2; arrType.type.accept(this); indent -= 2;
	}

	public void visit(AbsAtomConst atomConst) {
		switch (atomConst.type) {
		case AbsAtomConst.BOOL:
			Report.dump(indent, "AbsAtomConst " + atomConst.position.toString() + ": BOOLEAN(" + atomConst.value + ")");
			break;
		case AbsAtomConst.INT:
			Report.dump(indent, "AbsAtomConst " + atomConst.position.toString() + ": INTEGER(" + atomConst.value + ")");
			break;
		case AbsAtomConst.STR:
			Report.dump(indent, "AbsAtomConst " + atomConst.position.toString() + ": STRING(" + atomConst.value + ")");
			break;
		case AbsAtomConst.FLOAT:
			Report.dump(indent, "AbsAtomConst " + atomConst.position.toString() + ": FLOAT(" + atomConst.value + ")");
			break;
		case AbsAtomConst.CHAR:
			Report.dump(indent, "AbsAtomConst " + atomConst.position.toString() + ": CHAR(" + atomConst.value + ")");
			break;
		default:
			Report.error("Internal error :: compiler.abstr.Abstr.visit(AbsAtomConst)");
		}
	}

	public void visit(AbsAtomType atomType) {
		switch (atomType.type) {
		// TODO: CONST, AbsConstType?
		case AbsAtomType.BOOL:
			Report.dump(indent, "AbsAtomType " + atomType.position.toString() + ": BOOLEAN");
			break;
		case AbsAtomType.INT:
			Report.dump(indent, "AbsAtomType " + atomType.position.toString() + ": INTEGER");
			break;
		case AbsAtomType.FLOAT:
			Report.dump(indent, "AbsAtomType " + atomType.position.toString() + ": FLOAT");
			break;
		case AbsAtomType.CHAR:
			Report.dump(indent, "AbsAtomType " + atomType.position.toString() + ": CHAR");
			break;
		case AbsAtomType.VOID:
			Report.dump(indent, "AbsAtomType " + atomType.position.toString() + ": VOID");
			break;
		default:
			Report.error("Internal error :: compiler.abstr.Abstr.visit(AbsAtomType)");
		}
	}

	public void visit(AbsBinExpr binExpr) {
		switch (binExpr.oper) {
		case AbsBinExpr.OR:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": OR");
			break;
		case AbsBinExpr.AND:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": AND");
			break;
		case AbsBinExpr.EQU:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": EQU");
			break;
		case AbsBinExpr.NEQ:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": NEQ");
			break;
		case AbsBinExpr.LEQ:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": LEQ");
			break;
		case AbsBinExpr.GEQ:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": GEQ");
			break;
		case AbsBinExpr.LTH:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": LTH");
			break;
		case AbsBinExpr.GTH:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": GTH");
			break;
		case AbsBinExpr.ADD:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": ADD");
			break;
		case AbsBinExpr.SUB:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": SUB");
			break;
		case AbsBinExpr.MUL:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": MUL");
			break;
		case AbsBinExpr.DIV:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": DIV");
			break;
		case AbsBinExpr.MOD:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": MOD");
			break;
		case AbsBinExpr.DOT:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": DOT");
			break;
		case AbsBinExpr.ARR:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": ARR");
			break;
		case AbsBinExpr.ASSIGN:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": ASSIGN");
			break;
		case AbsBinExpr.BIT_OR:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": BIT_OR");
			break;
		case AbsBinExpr.BIT_XOR:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": BIT_XOR");
			break;
		case AbsBinExpr.BIT_AND:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": BIT_AND");
			break;
		case AbsBinExpr.LSHIFT:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": LSHIFT");
			break;
		case AbsBinExpr.RSHIFT:
			Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": RSHIFT");
			break;
		default:
			Report.error("Internal error :: compiler.abstr.Abstr.visit(AbsBinExpr)");
		}
		indent += 2; binExpr.expr1.accept(this); indent -= 2;
		indent += 2; binExpr.expr2.accept(this); indent -= 2;
	}

	public void visit(AbsComp comp) {
		Report.dump(indent, "AbsComp " + comp.position.toString() + ": " + comp.name);
		indent += 2; comp.type.accept(this); indent -= 2;
	}

	public void visit(AbsCompName compName) {
		Report.dump(indent, "AbsCompName " + compName.position.toString() + ": " + compName.name);
	}

	public void visit(AbsDefs defs) {
		Report.dump(indent, "AbsDefs " + defs.position.toString() + ":");
		for (int def = 0; def < defs.numDefs(); def++) {
			indent += 2; defs.def(def).accept(this); indent -= 2;
		}
	}

	public void visit(AbsDoWhile where) {
		Report.dump(indent, "AbsDoWhile " + where.position.toString() + ":");
		indent += 2; where.body.accept(this); indent -= 2;
		indent += 2; where.cond.accept(this); indent -= 2;
	}

	public void visit(AbsExprs exprs) {
		Report.dump(indent, "AbsExprs " + exprs.position.toString() + ":");
		for (int expr = 0; expr < exprs.numExprs(); expr++) {
			indent += 2; exprs.expr(expr).accept(this); indent -= 2;
		}
	}

	public void visit(AbsFor forStmt) {
		Report.dump(indent, "AbsFor " + forStmt.position.toString() + ":");
		indent += 2; forStmt.init.accept(this); indent -= 2;
		indent += 2; forStmt.cond.accept(this); indent -= 2;
		indent += 2; forStmt.step.accept(this); indent -= 2;
		indent += 2; forStmt.body.accept(this); indent -= 2;
	}

	public void visit(AbsFunCall funCall) {
		Report.dump(indent, "AbsFunCall " + funCall.position.toString() + ": " + funCall.name);
		for (int arg = 0; arg < funCall.numArgs(); arg++) {
			indent += 2; funCall.arg(arg).accept(this); indent -= 2;
		}
	}

	public void visit(AbsFunDef funDef) {
		Report.dump(indent, "AbsFunDef " + funDef.position.toString() + ": " + funDef.name);
		for (int par = 0; par < funDef.numPars(); par++) {
			indent += 2; funDef.par(par).accept(this); indent -= 2;
		}
		indent += 2; funDef.type.accept(this); indent -= 2;
		indent += 2; funDef.expr.accept(this); indent -= 2;
	}

	public void visit(AbsIf ifStmt) {
		Report.dump(indent, "AbsIf " + ifStmt.position.toString() + ":");
		indent += 2; ifStmt.cond.accept(this); indent -= 2;
		indent += 2; ifStmt.thenBody.accept(this); indent -= 2;
	}

	public void visit(AbsIfElse ifElse) {
		Report.dump(indent, "AbsIfElse " + ifElse.position.toString() + ":");
		indent += 2; ifElse.cond.accept(this); indent -= 2;
		indent += 2; ifElse.thenBody.accept(this); indent -= 2;
		indent += 2; ifElse.elseBody.accept(this); indent -= 2;
	}

	public void visit(AbsNop nop)
	{
		Report.dump(indent, "AbsNop " + nop.position.toString());
	}

	public void visit(AbsPar par) {
		Report.dump(indent, "AbsPar " + par.position.toString() + ": " + par.name);
		indent += 2; par.type.accept(this); indent -= 2;
	}

	public void visit(AbsPtrType ptrType) {
		Report.dump(indent, "AbsPtrType " + ptrType.position.toString() + ":");
		indent += 2; ptrType.type.accept(this); indent -= 2;
	}

	public void visit(AbsRecType recType) {
		Report.dump(indent, "AbsRecType " + recType.position.toString() + ":");
		for (int comp = 0; comp < recType.numComps(); comp++) {
			indent += 2; recType.comp(comp).accept(this); indent -= 2;
		}
	}

	public void visit(AbsReturn returnStmt) {
		Report.dump(indent, "AbsReturn " + returnStmt.position.toString());
		indent += 2; returnStmt.expr.accept(this); indent -= 2;
	}

	public void visit(AbsTypeDef typeDef) {
		Report.dump(indent, "AbsTypeDef " + typeDef.position.toString() + ": " + typeDef.name);
		indent += 2; typeDef.type.accept(this); indent -= 2;
	}

	public void visit(AbsTypeName typeName) {
		Report.dump(indent, "AbsTypeName " + typeName.position.toString() + ": " + typeName.name);
	}

	public void visit(AbsUnExpr unExpr) {
		switch (unExpr.oper) {
		case AbsUnExpr.ADD:
			Report.dump(indent, "AbsUnExpr " + unExpr.position.toString() + ": ADD");
			break;
		case AbsUnExpr.SUB:
			Report.dump(indent, "AbsUnExpr " + unExpr.position.toString() + ": SUB");
			break;
		case AbsUnExpr.MEM:
			Report.dump(indent, "AbsUnExpr " + unExpr.position.toString() + ": MEM");
			break;
		case AbsUnExpr.VAL:
			Report.dump(indent, "AbsUnExpr " + unExpr.position.toString() + ": VAL");
			break;
		case AbsUnExpr.NOT:
			Report.dump(indent, "AbsUnExpr " + unExpr.position.toString() + ": NOT");
			break;
		case AbsUnExpr.BIT_NOT:
			Report.dump(indent, "AbsUnExpr " + unExpr.position.toString() + ": BIT_NOT");
			break;
		case AbsUnExpr.INC:
			Report.dump(indent, "AbsUnExpr " + unExpr.position.toString() + ": INC");
			break;
		case AbsUnExpr.DEC:
			Report.dump(indent, "AbsUnExpr " + unExpr.position.toString() + ": DEC");
			break;
		default:
			Report.error("Internal error :: compiler.abstr.Abstr.visit(AbsUnExpr)");
		}
		indent += 2; unExpr.expr.accept(this); indent -= 2;
	}

	public void visit(AbsVarDef varDef) {
		Report.dump(indent, "AbsVarDef " + varDef.position.toString() + ": " + varDef.name);
		indent += 2; varDef.type.accept(this); if(varDef.expr != null) varDef.expr.accept(this); indent -= 2;
	}

	public void visit(AbsVarName varName) {
		Report.dump(indent, "AbsVarName " + varName.position.toString() + ": " + varName.name);
	}

	public void visit(AbsWhile where) {
		Report.dump(indent, "AbsWhile " + where.position.toString() + ":");
		indent += 2; where.cond.accept(this); indent -= 2;
		indent += 2; where.body.accept(this); indent -= 2;
	}
}
