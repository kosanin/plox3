package com.petar.plox3.parser;

public interface ExprVisitor<R> {
    R visitBinaryExpr(Expr.Binary expr);

    R visitUnaryExpr(Expr.Unary unary);

    R visitGroupingExpr(Expr.Grouping grouping);

    R visitLiteralExpr(Expr.Literal literal);

    R visitVariableExpr(Expr.Variable variable);

    R visitAssignmentExpr(Expr.Assignment assignment);

    R visitLogicalExpr(Expr.Logical logical);
}
