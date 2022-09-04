package compiler.seman;

import java.util.*;

import compiler.*;
import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.type.*;

public class SemAn implements Visitor {
	private final boolean dump;
	private int indent;

	public SemAn(boolean dump) {
		this.dump = dump;

		final Position position = new Position(0, 0);

		final Vector<AbsPar> intPars = new Vector<>();
		intPars.add(new AbsPar(position, "i", new AbsAtomType(position, AbsAtomType.INT)));

		final Vector<AbsPar> strPars = new Vector<>();
		strPars.add(new AbsPar(position, "i", new AbsAtomType(position, AbsAtomType.STR)));

		final Vector<AbsPar> getCharAtPars = new Vector<>();
		getCharAtPars.add(new AbsPar(position, "s", new AbsAtomType(position, AbsAtomType.STR)));
		getCharAtPars.add(new AbsPar(position, "i", new AbsAtomType(position, AbsAtomType.INT)));

		final Vector<AbsPar> putCharAtPars = new Vector<>();
		putCharAtPars.add(new AbsPar(position, "s", new AbsAtomType(position, AbsAtomType.STR)));
		putCharAtPars.add(new AbsPar(position, "c", new AbsAtomType(position, AbsAtomType.INT)));
		putCharAtPars.add(new AbsPar(position, "i", new AbsAtomType(position, AbsAtomType.INT)));

		final Vector<SemType> intParTypes = new Vector<>();
		intParTypes.add(new SemAtomType(SemAtomType.INT));

		final Vector<SemType> strParTypes = new Vector<>();
		strParTypes.add(new SemAtomType(SemAtomType.STR));

		final Vector<SemType> getCharAtParTypes = new Vector<>();
		getCharAtParTypes.add(new SemAtomType(SemAtomType.STR));
		getCharAtParTypes.add(new SemAtomType(SemAtomType.INT));

		final Vector<SemType> putCharAtParTypes = new Vector<>();
		putCharAtParTypes.add(new SemAtomType(SemAtomType.STR));
		putCharAtParTypes.add(new SemAtomType(SemAtomType.INT));
		putCharAtParTypes.add(new SemAtomType(SemAtomType.INT));

		final AbsAtomConst intConst = new AbsAtomConst(position, AbsAtomConst.INT, "0");
		final AbsAtomConst strConst = new AbsAtomConst(position, AbsAtomConst.STR, "");

		final AbsAtomType intType = new AbsAtomType(position, AbsAtomType.INT);
		final AbsAtomType strType = new AbsAtomType(position, AbsAtomType.STR);

		final AbsFunDef get_int = new AbsFunDef(position, "get_int", intPars, intType, intConst);
		final AbsFunDef put_int = new AbsFunDef(position, "put_int", intPars, intType, intConst);
		final AbsFunDef put_nl = new AbsFunDef(position, "put_nl", intPars, intType, intConst);
		final AbsFunDef get_str = new AbsFunDef(position, "get_str", strPars, strType, strConst);
		final AbsFunDef put_str = new AbsFunDef(position, "put_str", strPars, intType, intConst);
		final AbsFunDef get_char_at = new AbsFunDef(position, "get_char_at", getCharAtPars, intType, intConst);
		final AbsFunDef put_char_at = new AbsFunDef(position, "put_char_at", putCharAtPars, strType, strConst);

		final SemFunType intFun = new SemFunType(intParTypes, new SemAtomType(SemAtomType.INT));

		try {
			SymbTable.ins(get_int.name, get_int);
			SymbTable.ins(put_int.name, put_int);
			SymbTable.ins(put_nl.name, put_nl);
			SymbTable.ins(get_str.name, get_str);
			SymbTable.ins(put_str.name, put_str);
			SymbTable.ins(get_char_at.name, get_char_at);
			SymbTable.ins(put_char_at.name, put_char_at);

			SymbDesc.setType(get_int, intFun);
			SymbDesc.setType(put_int, intFun);
			SymbDesc.setType(put_nl, intFun);
			SymbDesc.setType(get_str, new SemFunType(strParTypes, new SemAtomType(SemAtomType.STR)));
			SymbDesc.setType(put_str, new SemFunType(strParTypes, new SemAtomType(SemAtomType.INT)));
			SymbDesc.setType(get_char_at, new SemFunType(getCharAtParTypes, new SemAtomType(SemAtomType.INT)));
			SymbDesc.setType(put_char_at, new SemFunType(putCharAtParTypes, new SemAtomType(SemAtomType.STR)));
		} catch (SemIllegalInsertException e) {
			Report.error("Internal error. Unable to add built-in functions to the symbol table.");
		}
	}

	public void dump(AbsTree tree) {
		if (!dump || Report.dumpFile() == null) {
			return;
		}

		indent = 0;
		tree.accept(this);
	}

	public void visit(AbsArrType arrType) {
		Report.dump(indent, "AbsArrType " + arrType.position.toString() + ": " + "[" + arrType.length + "]");

		{
			final SemType typ = SymbDesc.getType(arrType);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		arrType.type.accept(this);
		indent -= 2;
	}

	public void visit(AbsAtomConst atomConst) {
		switch (atomConst.type) {
			case AbsAtomConst.LOG:
				Report.dump(indent, "AbsAtomConst " + atomConst.position.toString() + ": LOGICAL(" + atomConst.value + ")");
				break;
			case AbsAtomConst.INT:
				Report.dump(indent, "AbsAtomConst " + atomConst.position.toString() + ": INTEGER(" + atomConst.value + ")");
				break;
			case AbsAtomConst.STR:
				Report.dump(indent, "AbsAtomConst " + atomConst.position.toString() + ": STRING(" + atomConst.value + ")");
				break;
			default:
				Report.error("Internal error :: compiler.abstr.Abstr.visit(AbsAtomConst)");
		}

		{
			final SemType typ = SymbDesc.getType(atomConst);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}
	}

	public void visit(AbsAtomType atomType) {
		switch (atomType.type) {
			case AbsAtomType.LOG:
				Report.dump(indent, "AbsAtomType " + atomType.position.toString() + ": LOGICAL");
				break;
			case AbsAtomType.INT:
				Report.dump(indent, "AbsAtomType " + atomType.position.toString() + ": INTEGER");
				break;
			case AbsAtomType.STR:
				Report.dump(indent, "AbsAtomType " + atomType.position.toString() + ": STRING");
				break;
			default:
				Report.error("Internal error :: compiler.abstr.Abstr.visit(AbsAtomType)");
		}

		{
			final SemType typ = SymbDesc.getType(atomType);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}
	}

	public void visit(AbsBinExpr binExpr) {
		switch (binExpr.oper) {
			case AbsBinExpr.IOR:
				Report.dump(indent, "AbsBinExpr " + binExpr.position.toString() + ": IOR");
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
			default:
				Report.error("Internal error :: compiler.abstr.Abstr.visit(AbsBinExpr)");
		}

		{
			final SemType typ = SymbDesc.getType(binExpr);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		binExpr.expr1.accept(this);
		binExpr.expr2.accept(this);
		indent -= 2;
	}

	public void visit(AbsComp comp) {
		Report.dump(indent, "AbsComp " + comp.position.toString() + ": " + comp.name);

		{
			final SemType typ = SymbDesc.getType(comp);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		comp.type.accept(this);
		indent -= 2;
	}

	public void visit(AbsCompName compName) {
		Report.dump(indent, "AbsCompName " + compName.position.toString() + ": " + compName.name);

		{
			final SemType typ = SymbDesc.getType(compName);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}
	}

	public void visit(AbsDefs defs) {
		Report.dump(indent, "AbsDefs " + defs.position.toString() + ":");

		{
			final SemType typ = SymbDesc.getType(defs);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		for (int def = 0; def < defs.numDefs(); def++) {
			indent += 2;
			defs.def(def).accept(this);
			indent -= 2;
		}
	}

	public void visit(AbsExprs exprs) {
		Report.dump(indent, "AbsExprs " + exprs.position.toString() + ":");

		{
			final SemType typ = SymbDesc.getType(exprs);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		for (int expr = 0; expr < exprs.numExprs(); expr++) {
			indent += 2;
			exprs.expr(expr).accept(this);
			indent -= 2;
		}
	}

	public void visit(AbsFor forStmt) {
		Report.dump(indent, "AbsFor " + forStmt.position.toString() + ":");

		{
			final SemType typ = SymbDesc.getType(forStmt);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		forStmt.count.accept(this);
		forStmt.lo.accept(this);
		forStmt.hi.accept(this);
		forStmt.step.accept(this);
		forStmt.body.accept(this);
		indent -= 2;
	}

	public void visit(AbsFunCall funCall) {
		Report.dump(indent, "AbsFunCall " + funCall.position.toString() + ": " + funCall.name);

		{
			final AbsDef def = SymbDesc.getNameDef(funCall);
			if (def != null) {
				Report.dump(indent + 2, "#defined at " + def.position.toString());
			}
		}

		{
			final SemType typ = SymbDesc.getType(funCall);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		for (int arg = 0; arg < funCall.numArgs(); arg++) {
			indent += 2;
			funCall.arg(arg).accept(this);
			indent -= 2;
		}
	}

	public void visit(AbsFunDef funDef) {
		Report.dump(indent, "AbsFunDef " + funDef.position.toString() + ": " + funDef.name);

		{
			SemType typ = SymbDesc.getType(funDef);
			if (typ != null)
				Report.dump(indent + 2, "#typed as " + typ);
		}

		for (int par = 0; par < funDef.numPars(); par++) {
			indent += 2;
			funDef.par(par).accept(this);
			indent -= 2;
		}

		indent += 2;
		funDef.type.accept(this);
		funDef.expr.accept(this);
		indent -= 2;
	}

	public void visit(AbsIfThen ifThen) {
		Report.dump(indent, "AbsIfThen " + ifThen.position.toString() + ":");

		{
			final SemType typ = SymbDesc.getType(ifThen);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		ifThen.cond.accept(this);
		ifThen.thenBody.accept(this);
		indent -= 2;
	}

	public void visit(AbsIfThenElse ifThenElse) {
		Report.dump(indent, "AbsIfThenElse " + ifThenElse.position.toString() + ":");

		{
			final SemType typ = SymbDesc.getType(ifThenElse);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		ifThenElse.cond.accept(this);
		ifThenElse.thenBody.accept(this);
		ifThenElse.elseBody.accept(this);
		indent -= 2;
	}

	public void visit(AbsPar par) {
		Report.dump(indent, "AbsPar " + par.position.toString() + ": " + par.name);

		{
			final SemType typ = SymbDesc.getType(par);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		par.type.accept(this);
		indent -= 2;
	}

	public void visit(AbsPtrType ptrType) {
		Report.dump(indent, "AbsPtrType " + ptrType.position.toString() + ":");

		{
			final SemType typ = SymbDesc.getType(ptrType);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		ptrType.type.accept(this);
		indent -= 2;
	}

	public void visit(AbsRecType recType) {
		Report.dump(indent, "AbsRecType " + recType.position.toString() + ":");

		{
			final SemType typ = SymbDesc.getType(recType);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		for (int comp = 0; comp < recType.numComps(); comp++) {
			indent += 2;
			recType.comp(comp).accept(this);
			indent -= 2;
		}
	}

	public void visit(AbsTypeDef typeDef) {
		Report.dump(indent, "AbsTypeDef " + typeDef.position.toString() + ": " + typeDef.name);

		{
			final SemType typ = SymbDesc.getType(typeDef);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		typeDef.type.accept(this);
		indent -= 2;
	}

	public void visit(AbsTypeName typeName) {
		Report.dump(indent, "AbsTypeName " + typeName.position.toString() + ": " + typeName.name);

		{
			final AbsDef def = SymbDesc.getNameDef(typeName);
			if (def != null) {
				Report.dump(indent + 2, "#defined at " + def.position.toString());
			}
		}

		{
			final SemType typ = SymbDesc.getType(typeName);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}
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
			default:
				Report.error("Internal error :: compiler.abstr.Abstr.visit(AbsBinExpr)");
		}

		{
			final SemType typ = SymbDesc.getType(unExpr);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		unExpr.expr.accept(this);
		indent -= 2;
	}

	public void visit(AbsVarDef varDef) {
		Report.dump(indent, "AbsVarDef " + varDef.position.toString() + ": " + varDef.name);

		{
			final SemType typ = SymbDesc.getType(varDef);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		varDef.type.accept(this);
		indent -= 2;
	}

	public void visit(AbsVarName varName) {
		Report.dump(indent, "AbsVarName " + varName.position.toString() + ": " + varName.name);

		{
			final AbsDef def = SymbDesc.getNameDef(varName);
			if (def != null) {
				Report.dump(indent + 2, "#defined at " + def.position.toString());
			}
		}
	}

	public void visit(AbsWhere where) {
		Report.dump(indent, "AbsWhere " + where.position.toString() + ":");

		{
			final SemType typ = SymbDesc.getType(where);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		where.expr.accept(this);
		where.defs.accept(this);
		indent -= 2;
	}

	public void visit(AbsWhile whileStmt) {
		Report.dump(indent, "AbsWhile " + whileStmt.position.toString() + ":");

		{
			final SemType typ = SymbDesc.getType(whileStmt);
			if (typ != null) {
				Report.dump(indent + 2, "#typed as " + typ);
			}
		}

		indent += 2;
		whileStmt.cond.accept(this);
		whileStmt.body.accept(this);
		indent -= 2;
	}
}
