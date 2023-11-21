package com.petar.plox3.scanner;

import java.util.HashMap;
import java.util.Map;

public class Keywords {

    protected static final Map<String, TokenType> MAP;

    static {
        MAP = new HashMap<>();
        MAP.put("and", TokenType.AND);
        MAP.put("class", TokenType.CLASS);
        MAP.put("else", TokenType.ELSE);
        MAP.put("false", TokenType.FALSE);
        MAP.put("for", TokenType.FOR);
        MAP.put("fun", TokenType.FUN);
        MAP.put("if", TokenType.IF);
        MAP.put("nil", TokenType.NIL);
        MAP.put("or", TokenType.OR);
        MAP.put("print", TokenType.PRINT);
        MAP.put("return", TokenType.RETURN);
        MAP.put("super", TokenType.SUPER);
        MAP.put("this", TokenType.THIS);
        MAP.put("true", TokenType.TRUE);
        MAP.put("var", TokenType.VAR);
        MAP.put("while", TokenType.WHILE);
    }
}
