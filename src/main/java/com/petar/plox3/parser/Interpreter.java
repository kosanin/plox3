package com.petar.plox3.parser;

import com.petar.plox3.Environment;
import com.petar.plox3.Plox3;
import com.petar.plox3.PloxCallable;
import com.petar.plox3.PloxFunction;
import com.petar.plox3.scanner.Token;
import com.petar.plox3.scanner.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {

    private Environment globals = new Environment();
    private Environment environment = globals;

    public Interpreter() {
        globals.define("clock", new PloxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter,
                               List<Object> arguments) {
                assert arguments.size() == 0;
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public Environment getGlobals() {
        return globals;
    }

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
    public Object visitVariableExpr(Expr.Variable variable) {
        return environment.get(variable.name());
    }

    @Override
    public Object visitAssignmentExpr(Expr.Assignment assignment) {
        Object value = evaluate(assignment.expression());
        environment.assign(assignment.name(), value);
        return value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical logical) {
        Object left = evaluate(logical.left());
        if (logical.operator().type() == TokenType.OR) {
            if (isTruthy(left)) {
                return left;
            }
        } else {
            // AND case
            // if left side is false, short-circuit and just return
            if (!isTruthy(left)) {
                return left;
            }
        }
        return evaluate(logical.right());
    }

    @Override
    public Object visitCallExpr(Expr.Call call) {
        Object callee = evaluate(call.callee());

        List<Object> args = new ArrayList<>();
        for (var arg : call.arguments()) {
            args.add(evaluate(arg));
        }

        if (!(callee instanceof PloxCallable)) {
            throw new RuntimeError(call.paren(),
                                   "Can only call functions and classes");
        }
        PloxCallable function = (PloxCallable) callee;
        if (args.size() != function.arity()) {
            throw new RuntimeError(call.paren(),
                                   "Expected %d arguments, got %d".formatted(
                                           function.arity(), args.size()));
        }
        return function.call(this, args);
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

    @Override
    public Void visitVarStatement(Stmt.VarStatement varStatement) {
        Object value = null;
        if (varStatement.expression() != null) {
            value = evaluate(varStatement.expression());
        }
        environment.define(varStatement.name().lexeme(), value);
        return null;
    }

    @Override
    public Void visitBlockStatement(Stmt.BlockStatement blockStatement) {
        executeBlock(blockStatement.statementList(),
                     new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStatement(Stmt.IfStmt ifStmt) {
        if (isTruthy(evaluate(ifStmt.condition()))) {
            execute(ifStmt.then());
        } else if (ifStmt.elseStmt() != null) {
            execute(ifStmt.elseStmt());
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Stmt.WhileStatement whileStatement) {
        while (isTruthy(evaluate(whileStatement.condition()))) {
            execute(whileStatement.body());
        }
        return null;
    }

    @Override
    public Void visitFunctionStatement(Stmt.Function stmt) {
        PloxFunction function = new PloxFunction(stmt);
        environment.define(stmt.name().lexeme(), function);
        return null;
    }

    public void executeBlock(List<Statement> statements,
                             Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (var stmt : statements) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
    }
}
