package com.petar.plox3.scanner;

import com.petar.plox3.Plox3;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int startOfTheToken = 0;
    private int currentCharacterPosition = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            startOfTheToken = currentCharacterPosition;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);
            case '!' -> addToken(
                    match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
            case '=' -> addToken(
                    match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '<' -> addToken(
                    match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>' -> addToken(
                    match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '/' -> {
                if (match('/')) {
                    // handle single line comments
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            case ' ', '\t', '\r' -> {
                // no-op for spaces
            }
            case '\n' -> line++;
            case '"' -> string();

            default -> {
                if (Character.isDigit(c)) {
                    number();
                } else if (Character.isAlphabetic(c)) {
                    identifier();
                } else {
                    Plox3.error(line, "Unexpected character.");
                }
            }
        }
    }

    private void identifier() {
        while (Character.isAlphabetic(peek()) || peek() == '_') {
            advance();
        }
        String text =
                source.substring(startOfTheToken, currentCharacterPosition);
        TokenType tokenType =
                Keywords.MAP.getOrDefault(text, TokenType.IDENTIFIER);

        addToken(tokenType);
    }

    private void number() {
        // read left of the decimal point
        while (Character.isDigit(peek())) {
            advance();
        }

        // read decimal point if exists
        if (peek() == '.' && Character.isDigit(peekNext())) {
            advance();

            // read right of the decimal point
            while (Character.isDigit(peek())) {
                advance();
            }
        }
        addToken(TokenType.NUMBER, Double.parseDouble(
                source.substring(startOfTheToken, currentCharacterPosition)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }
        if (isAtEnd()) {
            Plox3.error(line, "Unterminated string");
        }

        // consume closing "
        advance();

        addToken(TokenType.STRING, source.substring(startOfTheToken + 1,
                                                    currentCharacterPosition -
                                                            1));
    }

    private boolean match(char c) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(currentCharacterPosition) == c) {
            currentCharacterPosition++;
            return true;
        }
        return false;
    }

    private void addToken(TokenType tokenType, Object literal) {
        tokens.add(new Token(tokenType, source.substring(startOfTheToken,
                                                         currentCharacterPosition),
                             literal, line));
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private char advance() {
        return source.charAt(currentCharacterPosition++);
    }

    private char peekNext() {
        if (currentCharacterPosition + 1 > source.length()) {
            return '\0';
        }
        return source.charAt(currentCharacterPosition + 1);
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(currentCharacterPosition);
    }

    private boolean isAtEnd() {
        return currentCharacterPosition >= source.length();
    }

}
