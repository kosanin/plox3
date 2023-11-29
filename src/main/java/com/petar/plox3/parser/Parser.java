package com.petar.plox3.parser;

import com.petar.plox3.Plox3;
import com.petar.plox3.scanner.Token;
import com.petar.plox3.scanner.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int currentTokenIndex = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse() {
        List<Statement> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            stmts.add(declaration());
        }
        return stmts;
    }

    private Statement declaration() {
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Statement varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name");
        Expression initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expected ; after value.");
        return new Stmt.VarStatement(name, initializer);
    }

    private Statement statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        }
        if (match(TokenType.LEFT_BRACE)) {
            return new Stmt.BlockStatement(block());
        }
        if (match(TokenType.IF)) {
            return ifStatement();
        }
        if (match(TokenType.FOR)) {
            return forStatement();
        }
        if (match(TokenType.WHILE)) {
            return whileStatement();
        }
        return expressionStatement();
    }

    private Statement whileStatement() {
        consume(TokenType.LEFT_PAREN, "expect '(' before while condition");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "expect ')' after while condition");
        Statement body = statement();
        return new Stmt.WhileStatement(condition, body);
    }

    private Statement forStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' in for loop");
        Statement initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }
        Expression condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON,
                "Expected ';' after condition in for loop");

        Expression increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' in for loop");

        Statement body = statement();
        if (increment != null) {
            body = new Stmt.BlockStatement(
                    Arrays.asList(body, new Stmt.ExprStatement(increment)));
        }
        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.WhileStatement(condition, body);

        if (initializer != null) {
            body = new Stmt.BlockStatement(Arrays.asList(initializer, body));
        }
        return body;
    }

    private Statement ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' before if condition");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition");
        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.IfStmt(condition, thenBranch, elseBranch);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "expected end of block");
        return statements;
    }

    private Statement expressionStatement() {
        Expression expression = expression();
        consume(TokenType.SEMICOLON, "Expected ; after value.");
        return new Stmt.ExprStatement(expression);
    }

    private Statement printStatement() {
        Expression expression = expression();
        consume(TokenType.SEMICOLON, "Expected ; after value.");
        return new Stmt.PrintStatement(expression);
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        // need to parse lValue as it can be something like a.b().c().d
        Expression lValue = logicOr();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expression rValue = assignment();
            // verify lValue is a variable, e.g lValue = !x
            if (lValue instanceof Expr.Variable) {
                Token name = ((Expr.Variable) lValue).name();
                return new Expr.Assignment(name, rValue);
            }
            error(equals, "Invalid assignment target.");
        }
        return lValue;
    }

    private Expression logicOr() {
        Expression expression = logicAnd();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = logicAnd();
            expression = new Expr.Logical(expression, operator, right);
        }
        return expression;
    }

    private Expression logicAnd() {
        Expression expression = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equality();
            expression = new Expr.Logical(expression, operator, right);
        }
        return expression;
    }

    private Expression equality() {
        Expression expr = comparison();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Token previous() {
        return tokens.get(currentTokenIndex - 1);
    }

    private Expression comparison() {
        Expression expr = term();
        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER,
                     TokenType.GREATER_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expression term() {
        Expression expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expression right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expression factor() {
        Expression expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expression right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expression unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            return new Expr.Unary(operator, unary());
        }
        return primary();
    }

    private Expression primary() {
        if (match(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(TokenType.NIL)) {
            return new Expr.Literal(null);
        }
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal());
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expression expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ) but not found");
            return new Expr.Grouping(expr);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        throw error(peek(), "Expect expression.");
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type().equals(TokenType.SEMICOLON)) {
                return;
            }
            switch (peek().type()) {
                case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return;
                }
            }
            advance();
        }
    }

    private ParseError error(Token token, String message) {
        Plox3.error(token, message);
        return new ParseError();
    }

    private Token consume(TokenType tokenType, String message) {
        if (check(tokenType)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private boolean match(TokenType... types) {
        for (var tokenType : types) {
            if (tokens.get(currentTokenIndex).type().equals(tokenType)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType tokenType) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type().equals(tokenType);
    }

    private Token peek() {
        return tokens.get(currentTokenIndex);
    }

    private Token advance() {
        if (!isAtEnd()) {
            currentTokenIndex++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }
}
