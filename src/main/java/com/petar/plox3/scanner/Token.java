package com.petar.plox3.scanner;

public record Token(TokenType type, String lexeme, Object literal, int line) {}
