package com.petar.plox3;

import com.petar.plox3.parser.Interpreter;

import java.util.List;

public interface PloxCallable {
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
