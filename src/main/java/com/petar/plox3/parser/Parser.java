package com.petar.plox3.parser;

import com.petar.plox3.Plox3;
import com.petar.plox3.scanner.Token;
import com.petar.plox3.scanner.TokenType;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int currentTokenIndex = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expression parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expression expression() {
        return equality();
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

    private void consume(TokenType tokenType, String message) {
        if (check(tokenType)) {
            advance();
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
        return currentTokenIndex >= tokens.size();
    }
}