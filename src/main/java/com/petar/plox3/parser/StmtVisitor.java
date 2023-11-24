package com.petar.plox3.parser;

public interface StmtVisitor<R> {

    R visitPrintStatement(Stmt.PrintStatement printStatement);

    R visitExprStatement(Stmt.ExprStatement exprStatement);

    R visitVarStatement(Stmt.VarStatement varStatement);
}
