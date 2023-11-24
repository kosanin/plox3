package com.petar.plox3.parser;

public interface Statement {
    <R> R accept(StmtVisitor<R> visitor);
}
