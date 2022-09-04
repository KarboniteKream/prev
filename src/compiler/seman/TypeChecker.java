package compiler.seman;

import java.util.*;

import compiler.*;
import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.type.*;

public class TypeChecker implements Visitor {
	public void visit(AbsArrType acceptor) {
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, new SemArrType(acceptor.length, SymbDesc.getType(acceptor.type)));
	}

	public void visit(AbsAtomConst acceptor) {
		SymbDesc.setType(acceptor, new SemAtomType(acceptor.type));
	}

	public void visit(AbsAtomType acceptor) {
		SymbDesc.setType(acceptor, new SemAtomType(acceptor.type));
	}

	public void visit(AbsBinExpr acceptor) {
		acceptor.expr1.accept(this);
		acceptor.expr2.accept(this);

		final SemType type1 = SymbDesc.getType(acceptor.expr1);
		final SemType type2 = SymbDesc.getType(acceptor.expr2);

		final SemAtomType logical = new SemAtomType(SemAtomType.LOG);
		final SemAtomType integer = new SemAtomType(SemAtomType.INT);
		final SemAtomType string = new SemAtomType(SemAtomType.STR);

		switch (acceptor.oper) {
			case AbsBinExpr.IOR: case AbsBinExpr.AND:
				if (!type1.sameStructureAs(logical) || !type2.sameStructureAs(logical)) {
					Report.error(acceptor.position, "Expected (LOGICAL, LOGICAL), found (" + type1.actualType() + ", " + type2.actualType() + ").");
				}

				SymbDesc.setType(acceptor, logical);
			break;

			case AbsBinExpr.ADD: case AbsBinExpr.SUB:
			case AbsBinExpr.MUL: case AbsBinExpr.DIV:
			case AbsBinExpr.MOD:
				if (!type1.sameStructureAs(integer) || !type2.sameStructureAs(integer)) {
					Report.error(acceptor.position, "Expected (INTEGER, INTEGER), found (" + type1.actualType() + ", " + type2.actualType() + ").");
				}

				SymbDesc.setType(acceptor, integer);
			break;

			case AbsBinExpr.EQU: case AbsBinExpr.NEQ:
			case AbsBinExpr.LEQ: case AbsBinExpr.GEQ:
			case AbsBinExpr.LTH: case AbsBinExpr.GTH:
				if (type1.sameStructureAs(logical)) {
					if (!type2.sameStructureAs(logical)) {
						Report.error(acceptor.expr2.position, "Expected LOGICAL, found " + type2.actualType() + ".");
					}
				} else if (type1.sameStructureAs(integer)) {
					if (!type2.sameStructureAs(integer)) {
						Report.error(acceptor.expr2.position, "Expected INTEGER, found " + type2.actualType() + ".");
					}
				} else if (type1.actualType() instanceof SemPtrType) {
					if (!(type2.actualType() instanceof SemPtrType)) {
						Report.error(acceptor.expr2.position, "Expected PTR, found " + type2.actualType() + ".");
					}

					if (!type1.sameStructureAs(type2)) {
						Report.error(acceptor.expr2.position, "Expected " + type1.actualType() + ", found " + type2.actualType() + ".");
					}
				} else {
					Report.error(acceptor.expr1.position, "Expected { LOGICAL, INTEGER, PTR }, found " + type1.actualType() + ".");
				}

				SymbDesc.setType(acceptor, logical);
			break;

			case AbsBinExpr.ARR:
				if (!(type1.actualType() instanceof SemArrType) || !type2.sameStructureAs(integer)) {
					Report.error(acceptor.position, "Expected (ARR, INTEGER), found (" + type1.actualType() + ", " + type2.actualType() + ").");
				}

				SymbDesc.setType(acceptor, ((SemArrType) type1.actualType()).type);
			break;

			case AbsBinExpr.DOT:
				if (!(type1.actualType() instanceof SemRecType)) {
					Report.error(acceptor.expr1.position, "Expected REC, found " + type1.actualType() + ".");
				}

				final SemRecType record = (SemRecType) type1.actualType();

				for (int i = 0; i < record.getNumComps(); i++) {
					if (record.getCompName(i).equals(((AbsCompName) acceptor.expr2).name)) {
						SymbDesc.setType(acceptor.expr2, record.getCompType(i));
						SymbDesc.setType(acceptor, record.getCompType(i));
						return;
					}
				}

				Report.error(acceptor.expr2.position, "Undefined component " + ((AbsCompName) acceptor.expr2).name + ".");
			break;

			case AbsBinExpr.ASSIGN:
				if (type1.sameStructureAs(logical)) {
					if (!type2.sameStructureAs(type1)) {
						Report.error(acceptor.expr2.position, "Expected LOGICAL, found " + type2.actualType() + ".");
					}
				} else if (type1.sameStructureAs(integer)) {
					if (!type2.sameStructureAs(type1)) {
						Report.error(acceptor.expr2.position, "Expected INTEGER, found " + type2.actualType() + ".");
					}
				} else if (type1.sameStructureAs(string)) {
					if (!type2.sameStructureAs(type1)) {
						Report.error(acceptor.expr2.position, "Expected STRING, found " + type2.actualType() + ".");
					}
				} else if (type1.actualType() instanceof SemPtrType) {
					if (!(type2.actualType() instanceof SemPtrType)) {
						Report.error(acceptor.expr2.position, "Expected PTR, found " + type2.actualType() + ".");
					}

					if (!type1.sameStructureAs(type2)) {
						Report.error(acceptor.expr2.position, "Expected " + type1.actualType() + ", found " + type2.actualType() + ".");
					}
				} else {
					Report.error(acceptor.expr1.position, "Expected { LOGICAL, INTEGER, STRING, PTR }, found " + type1.actualType() + ".");
				}

				SymbDesc.setType(acceptor, type1);
			break;
		}
	}

	public void visit(AbsComp acceptor) {
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.type));
	}

	public void visit(AbsCompName acceptor) {}

	public void visit(AbsDefs acceptor) {
		AbsDef definition;

		for (int i = 0; i < acceptor.numDefs(); i++) {
			definition = acceptor.def(i);

			if (definition instanceof AbsTypeDef) {
				SymbDesc.setType(definition, new SemTypeName(((AbsTypeDef) definition).name));
			}
		}

		for (int i = 0; i < acceptor.numDefs(); i++) {
			definition = acceptor.def(i);

			if (definition instanceof AbsFunDef) {
				final AbsFunDef function = (AbsFunDef) definition;
				final Vector<SemType> parameters = new Vector<>();

				for (int j = 0; j < function.numPars(); j++) {
					function.par(j).accept(this);
					parameters.add(SymbDesc.getType(function.par(j)));
				}

				function.type.accept(this);
				SymbDesc.setType(function, new SemFunType(parameters, SymbDesc.getType(function.type)));
			} else {
				definition.accept(this);
			}
		}

		for (int i = 0; i < acceptor.numDefs(); i++) {
			definition = acceptor.def(i);

			if (definition instanceof AbsFunDef) {
				definition.accept(this);
			}
		}
	}

	public void visit(AbsExprs acceptor) {
		for (int i = 0; i < acceptor.numExprs(); i++) {
			acceptor.expr(i).accept(this);
		}

		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.expr(acceptor.numExprs() - 1)));
	}

	public void visit(AbsFor acceptor) {
		acceptor.count.accept(this);
		acceptor.lo.accept(this);
		acceptor.hi.accept(this);
		acceptor.step.accept(this);
		acceptor.body.accept(this);

		final SemAtomType integer = new SemAtomType(SemAtomType.INT);

		if (!SymbDesc.getType(acceptor.count).sameStructureAs(integer)) {
			Report.error(acceptor.count.position, "Expected INTEGER, found " + SymbDesc.getType(acceptor.count).actualType() + ".");
		}

		if (!SymbDesc.getType(acceptor.lo).sameStructureAs(integer)) {
			Report.error(acceptor.lo.position, "Expected INTEGER, found " + SymbDesc.getType(acceptor.lo).actualType() + ".");
		}

		if (!SymbDesc.getType(acceptor.hi).sameStructureAs(integer)) {
			Report.error(acceptor.hi.position, "Expected INTEGER, found " + SymbDesc.getType(acceptor.hi).actualType() + ".");
		}

		if (!SymbDesc.getType(acceptor.step).sameStructureAs(integer)) {
			Report.error(acceptor.step.position, "Expected INTEGER, found " + SymbDesc.getType(acceptor.step).actualType() + ".");
		}

		SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
	}

	public void visit(AbsFunCall acceptor) {
		final SemFunType type = (SemFunType) SymbDesc.getType(SymbDesc.getNameDef(acceptor));

		if (type.getNumPars() != acceptor.numArgs()) {
			Report.error(acceptor.position, "Expected " + type.getNumPars() + " parameter(s), found " + acceptor.numArgs() + ".");
		}

		for (int i = 0; i < acceptor.numArgs(); i++) {
			acceptor.arg(i).accept(this);

			if (!SymbDesc.getType(acceptor.arg(i)).sameStructureAs(type.getParType(i))) {
				Report.error(acceptor.arg(i).position, "Expected " + type.getParType(i).actualType() + ", found " + SymbDesc.getType(acceptor.arg(i)).actualType() + ".");
			}
		}

		SymbDesc.setType(acceptor, type.resultType);
	}

	public void visit(AbsFunDef acceptor) {
		acceptor.expr.accept(this);

		final SemType returnType = ((SemFunType) SymbDesc.getType(acceptor)).resultType;
		final SemType expression = SymbDesc.getType(acceptor.expr);

		if (!returnType.sameStructureAs(expression)) {
			Report.error(acceptor.expr.position, "Expected " + returnType.actualType() + ", found " + expression.actualType() + ".");
		}
	}

	public void visit(AbsIfThen acceptor) {
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);

		if (SymbDesc.getType(acceptor.cond).sameStructureAs(new SemAtomType(SemAtomType.LOG))) {
			SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
		} else {
			Report.error(acceptor.cond.position, "Expected LOGICAL, found " + SymbDesc.getType(acceptor.cond).actualType() + ".");
		}
	}

	public void visit(AbsIfThenElse acceptor) {
		acceptor.cond.accept(this);
		acceptor.thenBody.accept(this);
		acceptor.elseBody.accept(this);

		if (SymbDesc.getType(acceptor.cond).sameStructureAs(new SemAtomType(SemAtomType.LOG))) {
			SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
		} else {
			Report.error(acceptor.cond.position, "Expected LOGICAL, found " + SymbDesc.getType(acceptor.cond).actualType() + ".");
		}
	}

	public void visit(AbsPar acceptor) {
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.type));
	}

	public void visit(AbsPtrType acceptor) {
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, new SemPtrType(SymbDesc.getType(acceptor.type)));
	}

	public void visit(AbsRecType acceptor) {
		final Vector<String> names = new Vector<>();
		final Vector<SemType> types = new Vector<>();

		for (int i = 0; i < acceptor.numComps(); i++) {
			final AbsComp component = acceptor.comp(i);
			component.accept(this);
			names.add(component.name);
			types.add(SymbDesc.getType(component));
		}

		SymbDesc.setType(acceptor, new SemRecType(names, types));
	}

	public void visit(AbsTypeDef acceptor) {
		acceptor.type.accept(this);
		((SemTypeName) SymbDesc.getType(acceptor)).setType(SymbDesc.getType(acceptor.type));
	}

	public void visit(AbsTypeName acceptor) {
		SymbDesc.setType(acceptor, SymbDesc.getType(SymbDesc.getNameDef(acceptor)));
	}

	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);

		if (acceptor.oper == AbsUnExpr.NOT) {
			if (SymbDesc.getType(acceptor.expr).sameStructureAs(new SemAtomType(SemAtomType.LOG))) {
				SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.LOG));
			} else {
				Report.error(acceptor.expr.position, "Expected LOGICAL, found " + SymbDesc.getType(acceptor.expr).actualType() + ".");
			}
		} else if (acceptor.oper == AbsUnExpr.ADD || acceptor.oper == AbsUnExpr.SUB) {
			if (SymbDesc.getType(acceptor.expr).sameStructureAs(new SemAtomType(SemAtomType.INT))) {
				SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.INT));
			} else {
				Report.error(acceptor.expr.position, "Expected INTEGER, found " + SymbDesc.getType(acceptor.expr).actualType() + ".");
			}
		} else if (acceptor.oper == AbsUnExpr.MEM) {
			SymbDesc.setType(acceptor, new SemPtrType(SymbDesc.getType(acceptor.expr)));
		} else if (acceptor.oper == AbsUnExpr.VAL) {
			if (SymbDesc.getType(acceptor.expr).actualType() instanceof SemPtrType) {
				SymbDesc.setType(acceptor, ((SemPtrType) SymbDesc.getType(acceptor.expr).actualType()).type);
			} else {
				Report.error(acceptor.expr.position, "Expected PTR, found " + SymbDesc.getType(acceptor.expr).actualType() + ".");
			}
		}
	}

	public void visit(AbsVarDef acceptor) {
		acceptor.type.accept(this);
		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.type));
	}

	public void visit(AbsVarName acceptor) {
		SymbDesc.setType(acceptor, SymbDesc.getType(SymbDesc.getNameDef(acceptor)));
	}

	public void visit(AbsWhere acceptor) {
		acceptor.defs.accept(this);
		acceptor.expr.accept(this);
		SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.expr));
	}

	public void visit(AbsWhile acceptor) {
		acceptor.cond.accept(this);
		acceptor.body.accept(this);

		if (SymbDesc.getType(acceptor.cond).sameStructureAs(new SemAtomType(SemAtomType.LOG))) {
			SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
		} else {
			Report.error(acceptor.cond.position, "Expected LOGICAL, found " + SymbDesc.getType(acceptor.cond).actualType() + ".");
		}
	}
}
