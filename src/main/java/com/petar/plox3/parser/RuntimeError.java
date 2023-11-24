package com.petar.plox3.parser;

import com.petar.plox3.scanner.Token;

public class RuntimeError extends RuntimeException {
    private final Token token;

    public RuntimeError(Token operator, String message) {
        super(message);
        this.token = operator;
    }

    public Token getToken() {
        return token;
    }
}
