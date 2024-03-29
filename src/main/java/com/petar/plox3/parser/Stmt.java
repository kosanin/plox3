package com.petar.plox3.parser;

import com.petar.plox3.scanner.Token;

import java.util.List;

public record Stmt() {

    public record ExprStatement(Expression expression) implements Statement {
        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitExprStatement(this);
        }
    }

    public record PrintStatement(Expression expression) implements Statement {
        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitPrintStatement(this);
        }
    }

    public record VarStatement(Token name, Expression expression)
            implements Statement {
        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitVarStatement(this);
        }
    }

    public record BlockStatement(List<Statement> statementList)
            implements Statement {
        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    public record IfStmt(Expression condition, Statement then,
                         Statement elseStmt) implements Statement {

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitIfStatement(this);
        }
    }

    public record WhileStatement(Expression condition, Statement body)
            implements Statement {
        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }

    public record Function(Token name, List<Token> params, List<Statement> body)
            implements Statement {

        @Override
        public <R> R accept(StmtVisitor<R> visitor) {
            return visitor.visitFunctionStatement(this);
        }
    }
}
