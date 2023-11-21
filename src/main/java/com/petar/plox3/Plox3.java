package com.petar.plox3;

import com.petar.plox3.scanner.Scanner;
import com.petar.plox3.scanner.Token;

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
        for (var token : tokens) {
            System.out.println(token);
        }
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.out.printf("line=%d, error=%s, where=%s%n", line, message,
                          where);
        hadError = true;
    }

}
