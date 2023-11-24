package com.petar.plox3.parser;

import com.petar.plox3.Plox3;
import com.petar.plox3.scanner.Token;

import java.util.List;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {

    public void interpret(List<Statement> statements) {
        try {
            statements.forEach(this::execute);
        } catch (RuntimeError error) {
            Plox3.runtimeError(error);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }
        if (object instanceof Double) {
            String text = object.toString();
            // handle integers
            return trimTrailingZero(text);
        }
        return object.toString();
    }

    private String trimTrailingZero(String text) {
        if (text.endsWith(".0")) {
            return text.substring(0, text.length() - 2);
        }
        return text;
    }

    private Object evaluate(Expression expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left());
        Object right = evaluate(expr.right());
        switch (expr.operator().type()) {
            case MINUS -> {
                checkNumberOperands(expr.operator(), left, right);
                return (double) left - (double) right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return left + right.toString();

                }
                if (left instanceof Double && right instanceof String) {
                    return trimTrailingZero(left.toString()) + right;
                }
                if (left instanceof String && right instanceof Double) {
                    return left + trimTrailingZero(right.toString());
                }
                throw new RuntimeError(expr.operator(),
                                       "Operands must be numbers or " +
                                               "strings");
            }
            case STAR -> {
                checkNumberOperands(expr.operator(), left, right);
                return (double) left * (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator(), left, right);
                double rightTmp = (double) right;
                if (rightTmp == 0) {
                    throw new RuntimeError(expr.operator(), "Division by zero");
                }
                return (double) left / rightTmp;
            }
            case GREATER -> {
                checkNumberOperands(expr.operator(), left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator(), left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator(), left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator(), left, right);
                return (double) left <= (double) right;
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
        }
        return null;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null) {
            return false;
        }
        return left.equals(right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary unary) {
        Object eval = evaluate(unary.expr());
        switch (unary.operator().type()) {
            case BANG -> {
                return !isTruthy(eval);
            }
            case MINUS -> {
                checkNumberOperand(unary.operator(), eval);
                return -(double) eval;
            }
        }
        return null;
    }

    private void checkNumberOperands(Token operator, Object left,
                                     Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void checkNumberOperand(Token operator, Object eval) {
        if (eval instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number.");

    }

    private boolean isTruthy(Object eval) {
        if (eval == null) {
            return false;
        }
        if (eval instanceof Boolean) {
            return (boolean) eval;
        }
        return true;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping grouping) {
        return evaluate(grouping.expr());
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal literal) {
        return literal.value();
    }

    @Override
    public Void visitPrintStatement(Stmt.PrintStatement printStatement) {
        Object expr = evaluate(printStatement.expression());
        System.out.println(stringify(expr));
        return null;
    }

    @Override
    public Void visitExprStatement(Stmt.ExprStatement exprStatement) {
        evaluate(exprStatement.expression());
        return null;
    }
}
