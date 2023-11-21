package com.petar.plox3.parser;

public interface Expression {
    <R> R accept(ExprVisitor<R> visitor);
}
