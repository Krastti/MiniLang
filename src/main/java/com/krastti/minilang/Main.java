package com.krastti.minilang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Укажи путь к файлу .mini");
            return;
        }

        String source = Files.readString(Path.of(args[0]));
        var tokens = new Lexer().tokenize(source);
        var statements = new Parser().parse(tokens);
        new Interpretator().execute(statements, System.out);
    }
}
