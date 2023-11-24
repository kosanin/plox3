package com.petar.plox3.parser;

public class AstPrinter implements ExprVisitor<String> {

    public String print(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary binary) {
        return parenthesize(binary.operator().lexeme(), binary.left(),
                            binary.right());
    }

    @Override
    public String visitUnaryExpr(Expr.Unary unary) {
        return parenthesize(unary.operator().lexeme(), unary.expr());
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping grouping) {
        return parenthesize("grouping", grouping.expr());
    }

    @Override
    public String visitLiteralExpr(Expr.Literal literal) {
        return literal.value() != null ? literal.value().toString() : "nil";
    }

    @Override
    public String visitVariableExpr(Expr.Variable variable) {
        return variable.name().lexeme();
    }

    private String parenthesize(String name, Expression... expressions) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(").append(name);
        for (var expr : expressions) {
            stringBuilder.append(" ").append(expr.accept(this));
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
