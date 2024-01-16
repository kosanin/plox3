package com.petar.plox3.parser;

import com.petar.plox3.scanner.Token;

import java.util.List;

public record Expr() {

    public record Binary(Expression left, Token operator, Expression right)
            implements Expression {
        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public record Unary(Token operator, Expression expr) implements Expression {
        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public record Call(Expression callee, Token paren,
                       List<Expression> arguments) implements Expression {

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    public record Grouping(Expression expr) implements Expression {
        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public record Literal(Object value) implements Expression {
        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public record Variable(Token name) implements Expression {
        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    public record Assignment(Token name, Expression expression)
            implements Expression {

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitAssignmentExpr(this);
        }
    }

    public record Logical(Expression left, Token operator, Expression right)
            implements Expression {

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

}
