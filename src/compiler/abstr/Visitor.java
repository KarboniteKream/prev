package compiler.abstr;

import compiler.abstr.tree.*;

public interface Visitor
{
	public void visit(AbsArrType   acceptor);
	public void visit(AbsAtomConst acceptor);
	public void visit(AbsAtomType  acceptor);
	public void visit(AbsBinExpr   acceptor);
	public void visit(AbsComp      acceptor);
	public void visit(AbsCompName  acceptor);
	public void visit(AbsDefs      acceptor);
	public void visit(AbsDoWhile   acceptor);
    public void visit(AbsExprs     acceptor);
    public void visit(AbsFor       acceptor);
	public void visit(AbsFunCall   acceptor);
	public void visit(AbsFunDef    acceptor);
	public void visit(AbsIf        acceptor);
	public void visit(AbsIfElse    acceptor);
	public void visit(AbsNop       acceptor);
	public void visit(AbsPar       acceptor);
	public void visit(AbsPtrType   acceptor);
	public void visit(AbsRecType   acceptor);
	public void visit(AbsReturn    acceptor);
	public void visit(AbsTypeDef   acceptor);
	public void visit(AbsTypeName  acceptor);
	public void visit(AbsUnExpr    acceptor);
	public void visit(AbsVarDef    acceptor);
	public void visit(AbsVarName   acceptor);
	public void visit(AbsWhile     acceptor);
}
