package com.petar.plox3;

import com.petar.plox3.parser.Interpreter;
import com.petar.plox3.parser.Stmt;

import java.util.List;

public class PloxFunction implements PloxCallable {

    private final Stmt.Function declaration;

    public PloxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.getGlobals());
        for (int i = 0; i < declaration.params().size(); i++) {
            environment.define(declaration.params().get(i).lexeme(),
                               arguments.get(i));
        }
        interpreter.executeBlock(declaration.body(), environment);
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name().lexeme() + '>';
    }
}
