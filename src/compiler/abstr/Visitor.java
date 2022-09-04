package compiler.abstr;

import compiler.abstr.tree.*;

public interface Visitor {
	void visit(AbsArrType    acceptor);
	void visit(AbsAtomConst  acceptor);
	void visit(AbsAtomType   acceptor);
	void visit(AbsBinExpr    acceptor);
	void visit(AbsComp       acceptor);
	void visit(AbsCompName   acceptor);
	void visit(AbsDefs       acceptor);
    void visit(AbsExprs      acceptor);
    void visit(AbsFor        acceptor);
	void visit(AbsFunCall    acceptor);
	void visit(AbsFunDef     acceptor);
	void visit(AbsIfThen     acceptor);
	void visit(AbsIfThenElse acceptor);
	void visit(AbsPar        acceptor);
	void visit(AbsPtrType    acceptor);
	void visit(AbsRecType    acceptor);
	void visit(AbsTypeDef    acceptor);
	void visit(AbsTypeName   acceptor);
	void visit(AbsUnExpr     acceptor);
	void visit(AbsVarDef     acceptor);
	void visit(AbsVarName    acceptor);
	void visit(AbsWhere      acceptor);
	void visit(AbsWhile      acceptor);
}
