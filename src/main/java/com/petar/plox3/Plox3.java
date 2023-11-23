package com.petar.plox3;

import com.petar.plox3.parser.AstPrinter;
import com.petar.plox3.parser.Expression;
import com.petar.plox3.parser.Parser;
import com.petar.plox3.scanner.Scanner;
import com.petar.plox3.scanner.Token;
import com.petar.plox3.scanner.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Plox3 {
    private static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: plox3 <path_to_script>");
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runRepl();
        }
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void runRepl() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        while (true) {
            System.out.println("> ");
            String line = bufferedReader.readLine();
            if (line == null || line.equals("exit")) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, StandardCharsets.UTF_8));
        if (hadError) {
            System.exit(65);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        if (hadError) {
            return;
        }

        System.out.println(new AstPrinter().print(expression));
    }

    private static void report(int line, String where, String message) {
        System.out.printf("line=%d, error=%s, where=%s%n", line, message,
                          where);
        hadError = true;
    }

    public static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at the end ", message);
        } else {
            report(token.line(), " at " + token.lexeme() + " ", message);
        }
    }
}
