package com.petar.plox3;

import com.petar.plox3.parser.RuntimeError;
import com.petar.plox3.scanner.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }
        throw new RuntimeError(name, "Undefined variable: " + name.lexeme());
    }
}
